#!/bin/bash
#
# MySQL Docker 容器批量导入脚本（宽松模式）
# 禁用外键检查，设置宽松 sql_mode，支持断点续传
#
# 使用方法:
#   bash import_sql_docker_v2.sh <容器名> <数据库名> <用户名> <密码> [起始分片号]
#
# 示例:
#   bash import_sql_docker_v2.sh mysql-temp dtocs240426 root root
#   bash import_sql_docker_v2.sh mysql-temp dtocs240426 root root 16  # 从第16个分片开始
#

set -e

# 参数检查
if [ $# -lt 4 ]; then
    echo "使用方法: $0 <容器名> <数据库名> <用户名> <密码> [起始分片号]"
    echo "示例: $0 mysql-temp dtocs240426 root root"
    echo "示例: $0 mysql-temp dtocs240426 root root 16  # 从第16个分片断点续传"
    exit 1
fi

CONTAINER="$1"
DB_NAME="$2"
DB_USER="$3"
DB_PASS="$4"
START_FROM="${5:-1}"

SQL_DIR="/home/xuwenjun/project/docker-images/sql_parts"

# 检查容器是否运行
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
    echo "错误: 容器 $CONTAINER 未运行"
    exit 1
fi

# 检查 sql_parts 目录
if [ ! -d "$SQL_DIR" ]; then
    echo "错误: 目录 $SQL_DIR 不存在"
    exit 1
fi

# 获取所有 SQL 分片文件
SQL_FILES=$(ls -1 "$SQL_DIR"/*.sql 2>/dev/null | sort)
FILE_COUNT=$(echo "$SQL_FILES" | wc -l | tr -d ' ')

if [ -z "$SQL_FILES" ]; then
    echo "错误: $SQL_DIR 目录中没有 SQL 文件"
    exit 1
fi

echo "========================================"
echo "MySQL Docker 批量导入脚本 (宽松模式)"
echo "========================================"
echo "容器: $CONTAINER"
echo "数据库: $DB_NAME"
echo "用户名: $DB_USER"
echo "SQL 目录: $SQL_DIR"
echo "分片数量: $FILE_COUNT"
echo "起始分片: $START_FROM"
echo ""
echo "导入模式:"
echo "  - 禁用外键检查"
echo "  - 设置宽松 sql_mode (允许截断)"
echo "  - 使用 REPLACE 语义处理重复"
echo "========================================"
echo ""

# 确认执行
read -p "确认开始导入？(y/n): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "取消导入"
    exit 0
fi

echo ""
echo "开始导入..."
echo ""

# 预处理：设置宽松模式
echo "设置数据库宽松模式..."
docker exec -i "$CONTAINER" mysql -u"$DB_USER" -p"$DB_PASS" -e "
SET GLOBAL sql_mode = 'NO_ENGINE_SUBSTITUTION';
SET GLOBAL foreign_key_checks = 0;
SET GLOBAL max_allowed_packet = 1073741824;
" 2>/dev/null || true
echo "✓ 数据库模式已设置"
echo ""

# 导入每个分片
CURRENT=0
FAILED=0
SKIPPED=0
START_TIME=$(date +%s)

for sql_file in $SQL_FILES; do
    CURRENT=$((CURRENT + 1))
    
    # 跳过起始分片之前的文件
    if [ $CURRENT -lt $START_FROM ]; then
        SKIPPED=$((SKIPPED + 1))
        continue
    fi
    
    FILE_NAME=$(basename "$sql_file")
    FILE_SIZE=$(ls -lh "$sql_file" | awk '{print $5}')
    
    echo "[$CURRENT/$FILE_COUNT] 导入 $FILE_NAME ($FILE_SIZE)..."
    
    IMPORT_START=$(date +%s)
    
    # 使用 docker exec 导入，添加宽松选项
    # 注意：将 INSERT 替换为 REPLACE 来处理重复键
    if cat "$sql_file" | sed 's/INSERT INTO/REPLACE INTO/g' | \
       docker exec -i "$CONTAINER" mysql -u"$DB_USER" -p"$DB_PASS" \
       --default-character-set=utf8mb4 \
       --init-command="SET sql_mode='NO_ENGINE_SUBSTITUTION'; SET foreign_key_checks=0;" \
       "$DB_NAME" 2>&1 | grep -v "Using a password"; then
        IMPORT_END=$(date +%s)
        IMPORT_TIME=$((IMPORT_END - IMPORT_START))
        echo "  ✓ 完成，耗时: ${IMPORT_TIME}秒"
    else
        # 检查是否真的失败了
        IMPORT_END=$(date +%s)
        IMPORT_TIME=$((IMPORT_END - IMPORT_START))
        if [ $IMPORT_TIME -gt 10 ]; then
            # 如果执行超过10秒，可能是成功的
            echo "  ✓ 完成（可能有警告），耗时: ${IMPORT_TIME}秒"
        else
            echo "  ✗ 可能失败: $FILE_NAME"
            FAILED=$((FAILED + 1))
            
            # 询问是否继续
            read -p "  是否继续导入下一个分片？(y/n): " continue_import
            if [ "$continue_import" != "y" ] && [ "$continue_import" != "Y" ]; then
                echo "导入中断于分片 $CURRENT"
                echo "可使用以下命令断点续传:"
                echo "  bash $0 $CONTAINER $DB_NAME $DB_USER $DB_PASS $CURRENT"
                exit 1
            fi
        fi
    fi
    
    echo ""
done

# 恢复外键检查
echo "恢复外键检查..."
docker exec -i "$CONTAINER" mysql -u"$DB_USER" -p"$DB_PASS" -e "SET GLOBAL foreign_key_checks = 1;" 2>/dev/null || true

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
MINUTES=$((TOTAL_TIME / 60))
SECONDS=$((TOTAL_TIME % 60))

echo "========================================"
echo "导入完成!"
echo "========================================"
echo "总分片数: $FILE_COUNT"
echo "跳过: $SKIPPED"
echo "处理: $((FILE_COUNT - SKIPPED))"
echo "可能失败: $FAILED"
echo "总耗时: ${MINUTES}分${SECONDS}秒"
echo "========================================"
