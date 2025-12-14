print("开始测试...")
try:
    import docx
    print("✅ docx 库已安装")
except ImportError:
    print("❌ docx 库未安装，正在安装...")
    import subprocess
    import sys
    subprocess.check_call([sys.executable, "-m", "pip", "install", "python-docx", "-i", "https://pypi.tuna.tsinghua.edu.cn/simple"])
    print("✅ docx 库安装完成")
    import docx

print("测试完成！")
