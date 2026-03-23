#!/bin/bash
#
# MySQL 批量导入脚本
# 按顺序导入 sql_parts 目录下的所有 SQL 分片
#
# 使用方法:
#   bash import_sql.sh <数据库名> <用户名> <密码> [主机] [端口]
#
# 示例:
#   bash import_sql.sh mydb root password123
#   bash import_sql.sh mydb root password123 localhost 3306
#

set -e

# 参数检查
if [ $# -lt 3 ]; then
    echo "使用方法: $0 <数据库名> <用户名> <密码> [主机] [端口]"
    echo "示例: $0 mydb root password123 localhost 3306"
    exit 1
fi

DB_NAME="$1"
DB_USER="$2"
DB_PASS="$3"
DB_HOST="${4:-localhost}"
DB_PORT="${5:-3306}"

SQL_DIR="sql_parts"

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
echo "MySQL 批量导入脚本"
echo "========================================"
echo "数据库: $DB_NAME"
echo "用户名: $DB_USER"
echo "主机: $DB_HOST:$DB_PORT"
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
    
    # 使用 mysql 命令导入
    if mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$sql_file" 2>&1; then
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
