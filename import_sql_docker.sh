#!/bin/bash
#
# MySQL Docker 容器批量导入脚本
# 通过 docker exec 导入 sql_parts 目录下的所有 SQL 分片
#
# 使用方法:
#   bash import_sql_docker.sh <容器名> <数据库名> <用户名> <密码>
#
# 示例:
#   bash import_sql_docker.sh mysql-temp dtocs240426 root root
#

set -e

# 参数检查
if [ $# -lt 4 ]; then
    echo "使用方法: $0 <容器名> <数据库名> <用户名> <密码>"
    echo "示例: $0 mysql-temp dtocs240426 root root"
    exit 1
fi

CONTAINER="$1"
DB_NAME="$2"
DB_USER="$3"
DB_PASS="$4"

SQL_DIR="sql_parts"

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
echo "MySQL Docker 批量导入脚本"
echo "========================================"
echo "容器: $CONTAINER"
echo "数据库: $DB_NAME"
echo "用户名: $DB_USER"
echo "SQL 目录: $SQL_DIR"
echo "分片数量: $FILE_COUNT"
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

# 导入每个分片
CURRENT=0
FAILED=0
START_TIME=$(date +%s)

for sql_file in $SQL_FILES; do
    CURRENT=$((CURRENT + 1))
    FILE_NAME=$(basename "$sql_file")
    FILE_SIZE=$(ls -lh "$sql_file" | awk '{print $5}')
    
    echo "[$CURRENT/$FILE_COUNT] 导入 $FILE_NAME ($FILE_SIZE)..."
    
    IMPORT_START=$(date +%s)
    
    # 使用 docker exec 导入
    if docker exec -i "$CONTAINER" mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$sql_file" 2>&1; then
        IMPORT_END=$(date +%s)
        IMPORT_TIME=$((IMPORT_END - IMPORT_START))
        echo "  ✓ 完成，耗时: ${IMPORT_TIME}秒"
    else
        echo "  ✗ 导入失败: $FILE_NAME"
        FAILED=$((FAILED + 1))
        
        # 询问是否继续
        read -p "  是否继续导入下一个分片？(y/n): " continue_import
        if [ "$continue_import" != "y" ] && [ "$continue_import" != "Y" ]; then
            echo "导入中断"
            exit 1
        fi
    fi
    
    echo ""
done

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
MINUTES=$((TOTAL_TIME / 60))
SECONDS=$((TOTAL_TIME % 60))

echo "========================================"
echo "导入完成!"
echo "========================================"
echo "总分片数: $FILE_COUNT"
echo "成功: $((FILE_COUNT - FAILED))"
echo "失败: $FAILED"
echo "总耗时: ${MINUTES}分${SECONDS}秒"
echo "========================================"
