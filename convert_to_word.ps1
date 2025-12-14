# PowerShell脚本：将Markdown转换为Word

# 读取Markdown文件
$mdFile = "e:\tudianershatest\第3章_WEB开发技术点应用情况.md"
$docxFile = "e:\tudianershatest\第3章_WEB开发技术点应用情况.docx"

Write-Host "正在读取Markdown文件..." -ForegroundColor Green
$content = Get-Content -Path $mdFile -Raw

# 创建Word应用程序对象
Write-Host "正在创建Word文档..." -ForegroundColor Green
$word = New-Object -ComObject Word.Application
$word.Visible = $false

# 创建新文档
$doc = $word.Documents.Add()
$selection = $word.Selection

# 设置默认字体
$selection.Font.Name = "宋体"
$selection.Font.Size = 12

# 按行处理内容
$lines = $content -split "`n"
$inCodeBlock = $false

foreach ($line in $lines) {
    # 处理代码块
    if ($line -match '^```') {
        $inCodeBlock = -not $inCodeBlock
        continue
    }
    
    if ($inCodeBlock) {
        # 代码块内容
        $selection.Font.Name = "Consolas"
        $selection.Font.Size = 10
        $selection.TypeText($line)
        $selection.TypeParagraph()
        $selection.Font.Name = "宋体"
        $selection.Font.Size = 12
        continue
    }
    
    # 处理标题
    if ($line -match '^(#{1,6})\s+(.+)$') {
        $level = $Matches[1].Length
        $text = $Matches[2]
        
        $selection.Style = "标题 $level"
        $selection.Font.Name = "微软雅黑"
        $selection.Font.Bold = $true
        $selection.TypeText($text)
        $selection.TypeParagraph()
        $selection.Font.Bold = $false
        continue
    }
    
    # 处理普通段落
    if ($line.Trim() -ne "") {
        # 移除Markdown标记
        $cleanLine = $line -replace '\*\*(.+?)\*\*', '$1'
        $cleanLine = $cleanLine -replace '`(.+?)`', '$1'
        
        $selection.TypeText($cleanLine)
        $selection.TypeParagraph()
    } else {
        $selection.TypeParagraph()
    }
}

# 保存文档
Write-Host "正在保存Word文档..." -ForegroundColor Green
$doc.SaveAs([ref]$docxFile)
$doc.Close()
$word.Quit()

# 释放COM对象
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($selection) | Out-Null
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($doc) | Out-Null
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
[System.GC]::Collect()
[System.GC]::WaitForPendingFinalizers()

Write-Host "✅ 转换成功！Word文档已保存到: $docxFile" -ForegroundColor Green
