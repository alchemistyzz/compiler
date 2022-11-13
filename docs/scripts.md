# 快速比对脚本

在完成实验时, 我们可能会反复执行程序, 输出结果, 然后比对标准输出. 然而人眼比对文本是一个较为繁琐的过程, 我们在 `scripts` 文件夹下提供了两个快速比对的工具. 

## `diff.py`

Usage:
```shell
python -u diff.py /path/for/stdfile /path/for/srcfile
```

比对 `/path/for/stdfile` 与 `/path/for/srcfile` 的差异, 忽略文件尾空行和行首位空白符. 若有差异则输出首行不匹配. 

## `check-result.py`

Usage:
```shell
python -u check-result.py n /path/for/stdfiles /path/for/outputfiles
```

其中 n 为实验 n 的序号, 如 4 代表 实验四; `/path/for/stdfiles` 与 `/path/for/outputfiles` 是标准文件的文件夹和输出文件的文件夹. 

此脚本会比对本次实验和之前的所有实验的输出文件和标准文件的差异. 特别地, 对于实验四, 我们直接输出了 rars 的输出, 需要自行和程序的预期返回值比对. 

脚本中写死了 `rars.jar` 的路径, 需要修改为你电脑上 `rars.jar` 的路径:
```python
rars_path = "/home/test/rars.jar"
```