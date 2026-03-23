#!/usr/bin/env python3
"""通过 docker exec + taos 客户端管道方式导出 TDengine 数据"""
import subprocess
import re
import sys

CONTAINER = "tdengine-2.6.0.34"
OUTPUT = "/Volumes/TP302/tk_tdengine_dump.sql"
TABLES = [f"etth1_node0{i}" for i in range(1, 8)]

def taos_query(sql):
    """通过 docker exec 执行 taos 查询"""
    cmd = ["docker", "exec", CONTAINER, "taos", "-s", f"USE tk; {sql}"]
    result = subprocess.run(cmd, capture_output=True, text=True)
    return result.stdout

def get_create_table(table):
    """获取子表的建表语句"""
    output = taos_query(f"SHOW CREATE TABLE {table}\\G;")
    for line in output.split("\n"):
        if "Create Table:" in line:
            return line.replace("Create Table: ", "").strip()
    return None

def export_data(table):
    """导出单个子表数据，返回 INSERT 语句列表"""
    output = taos_query(f"SELECT ts, temp, tod, dow, dom, doy FROM {table};")
    rows = []
    for line in output.split("\n"):
        line = line.strip()
        if not line or line.startswith("=") or line.startswith("Query") or line.startswith("taos") or line.startswith("Welcome") or line.startswith("Copyright") or "Database changed" in line:
            continue
        if "ts" in line and "temp" in line and "tod" in line:
            continue
        # 解析数据行: 用 | 分隔
        parts = [p.strip() for p in line.split("|") if p.strip()]
        if len(parts) >= 6 and re.match(r'\d{4}-\d{2}-\d{2}', parts[0]):
            ts, temp, tod, dow, dom, doy = parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]
            rows.append(f"('{ts}', {temp}, {tod}, {dow}, {dom}, {doy})")
    return rows

def main():
    print(f"开始导出 TDengine tk 数据库...")

    with open(OUTPUT, "w", encoding="utf-8") as f:
        f.write("-- TDengine tk database dump (2.6 -> 3.x compatible)\n")
        f.write("CREATE DATABASE IF NOT EXISTS tk;\n")
        f.write("USE tk;\n\n")

        # 超级表
        f.write("CREATE STABLE IF NOT EXISTS etth1_metric "
                "(ts TIMESTAMP, temp DOUBLE, tod TINYINT, dow TINYINT, dom TINYINT, doy SMALLINT) "
                "TAGS (node_id TINYINT, node_code BINARY(16), location BINARY(32), remark BINARY(64));\n\n")

        # 子表建表语句
        for tbl in TABLES:
            create_sql = get_create_table(tbl)
            if create_sql:
                f.write(f"{create_sql};\n")
                print(f"  {tbl}: 获取建表语句成功")
        f.write("\n")

        # 导出数据
        total = 0
        for tbl in TABLES:
            print(f"  导出 {tbl} ...")
            rows = export_data(tbl)
            print(f"    获取 {len(rows)} 条记录")
            
            # 每 100 条一个 INSERT
            for i in range(0, len(rows), 100):
                batch = rows[i:i+100]
                f.write(f"INSERT INTO {tbl} VALUES {' '.join(batch)};\n")
            total += len(rows)

        f.write("\n-- Dump complete\n")

    import os
    size_mb = os.path.getsize(OUTPUT) / 1024 / 1024
    print(f"\n导出完成！总计 {total} 条记录")
    print(f"文件: {OUTPUT} ({size_mb:.1f} MB)")

if __name__ == "__main__":
    main()
