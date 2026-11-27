import runpy

p = 'patch_account.py'
t = open(p, encoding='utf-8').read()
t = t.replace("if '「' in s:", "if '「清除' in s:")
open(p, 'w', encoding='utf-8').write(t)
runpy.run_path(p, run_name='__main__')
