#!/usr/bin/env python3
"""
SQL 文件切分脚本
按语句边界（以分号+换行结尾）切分大型 SQL 文件，确保不切断单个 SQL 语句。
"""

import os
import sys

def split_sql_file(input_file, output_dir, max_size_gb=1):
    """
    按语句边界切分 SQL 文件
    
    Args:
        input_file: 输入的 SQL 文件路径
        output_dir: 输出目录
        max_size_gb: 每个分片的最大大小（GB）
    """
    max_size_bytes = max_size_gb * 1024 * 1024 * 1024  # 转换为字节
    
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    file_index = 1
    current_size = 0
    current_file = None
    base_name = os.path.splitext(os.path.basename(input_file))[0]
    
    total_size = os.path.getsize(input_file)
    processed_size = 0
    
    def open_new_file():
        nonlocal file_index, current_size, current_file
        if current_file:
            current_file.close()
            print(f"  完成分片 {file_index - 1}，大小: {current_size / (1024*1024*1024):.2f} GB")
        
        output_path = os.path.join(output_dir, f"{base_name}_part{file_index:03d}.sql")
        print(f"创建分片 {file_index}: {output_path}")
        current_file = open(output_path, 'w', encoding='utf-8')
        current_size = 0
        file_index += 1
        return current_file
    
    print(f"开始切分 SQL 文件: {input_file}")
    print(f"文件大小: {total_size / (1024*1024*1024):.2f} GB")
    print(f"目标分片大小: {max_size_gb} GB")
    print(f"输出目录: {output_dir}")
    print("-" * 50)
    
    current_file = open_new_file()
    buffer = ""
    
    with open(input_file, 'r', encoding='utf-8', errors='replace') as f:
        while True:
            chunk = f.read(10 * 1024 * 1024)  # 每次读取 10MB
            if not chunk:
                break
            
            processed_size += len(chunk.encode('utf-8'))
            buffer += chunk
            
            # 查找完整的 SQL 语句（以 ;\n 结尾）
            while True:
                # 查找语句结束位置
                pos = buffer.find(';\n')
                if pos == -1:
                    break
                
                # 提取完整语句（包含分号和换行）
                statement = buffer[:pos + 2]
                buffer = buffer[pos + 2:]
                
                statement_bytes = len(statement.encode('utf-8'))
                
                # 检查是否需要切换到新文件
                if current_size + statement_bytes > max_size_bytes and current_size > 0:
                    current_file = open_new_file()
                
                current_file.write(statement)
                current_size += statement_bytes
            
            # 显示进度
            progress = (processed_size / total_size) * 100
            print(f"\r进度: {progress:.1f}% ({processed_size / (1024*1024*1024):.2f} GB / {total_size / (1024*1024*1024):.2f} GB)", end="", flush=True)
    
    # 写入剩余内容
    if buffer:
        buffer_bytes = len(buffer.encode('utf-8'))
        if current_size + buffer_bytes > max_size_bytes and current_size > 0:
            current_file = open_new_file()
        current_file.write(buffer)
        current_size += buffer_bytes
    
    if current_file:
        current_file.close()
        print(f"\n  完成分片 {file_index - 1}，大小: {current_size / (1024*1024*1024):.2f} GB")
    
    print("-" * 50)
    print(f"切分完成！共生成 {file_index - 1} 个分片")
    
    # 列出所有分片
    print("\n生成的分片文件:")
    for f in sorted(os.listdir(output_dir)):
        if f.endswith('.sql'):
            path = os.path.join(output_dir, f)
            size = os.path.getsize(path)
            print(f"  {f}: {size / (1024*1024*1024):.2f} GB")

if __name__ == "__main__":
    input_file = sys.argv[1] if len(sys.argv) > 1 else "backup.sql"
    output_dir = sys.argv[2] if len(sys.argv) > 2 else "sql_parts"
    max_size = float(sys.argv[3]) if len(sys.argv) > 3 else 1.0
    
    if not os.path.exists(input_file):
        print(f"错误: 文件不存在 {input_file}")
        sys.exit(1)
    
    split_sql_file(input_file, output_dir, max_size)
