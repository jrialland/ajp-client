#!/usr/bin/env python
# -*- Codec: utf-8 -*-
import sys, os, os.path, codecs, re, fnmatch, subprocess, shutil

project_dir = '../'

path_to_eclipse=None
if path_to_eclipse is None and 'linux' in sys.platform:
	import subprocess
	try:
		path_to_eclipse = subprocess.check_output(['which', 'eclipse'])
	except subprocess.CalledProcessError:
		print 'could find eclipse executable, java indentation will not work'

def keep_backup_file(fnct):
    def wrapperfnct(*args, **kwargs):
        filename = args[0]
        print 'applying ' + fnct.__name__ + ' on ' + filename
        if os.path.isfile(filename):
            shutil.copyfile(filename, filename+'~')
            try:
                result = fnct(*args, **kwargs)
                os.remove(filename+'~')
                return result
            except Exception, e:
                print 'rollbacking '+filename
                os.remove(filename)
                shutil.copy(filename+'~', filename)
                raise e
    return wrapperfnct

@keep_backup_file
def convert_to_utf8(filename):
    with file(filename, 'r') as thefile:
        f = thefile.read()
    data = None
    for enc in ['utf-8', 'windows-1252', 'iso-8859-15']:
        try:
            data = f.decode(enc)
            break
        except Exception:
            continue
    if data is None:
        raise Exception('Could not read %s with any encoding' % (filename,))
    with open(filename, 'w') as thefile:
    	data = data.encode('utf-8')
    	data = re.sub('\r\n', '\n', data)
        thefile.write(data)

@keep_backup_file
def xmllint(filename):
    if os.path.isfile(filename):
        import xml.dom.minidom
        dom = xml.dom.minidom.parse(filename)
        pretty = dom.toprettyxml()
        pretty = '\n'.join([l for l in pretty.split('\n') if l.strip() <> '']) + '\n'
        with file(filename, 'w') as f:
            f.write(pretty)

@keep_backup_file
def dos2unix(filename):
    with file(filename) as f:
        data = f.read().replace('\r\n', '\n')
    with file(filename,'w') as f:
        f.write(data)
            
def to_java_comments(str, isJavadoc=False):
	lines = [s for s in str.split('\n')]
	c = ('/** ' if isJavadoc else '/* ') + lines[0] + '\n'
	for s in lines[1:]:
		c = c + ' * ' + s + '\n'
	c = c + ' */\n'
	return c

@keep_backup_file
def replace_java_header(filename, header):
	with file(filename, 'r') as f:
		content = f.read()
	
	for s in ['package ', 'import ', 'public ', 'protected ', 'class ', 'enum '] :
		index = -1
		try :
			index = content.index(s)
		except ValueError:
			continue
		if index > -1:
			out = codecs.open(filename, 'w', 'utf-8')
			content = to_java_comments(header) + content[index:]
			out.write(content)
			out.close()
			break

def format_java_files(configfile, path):
	cmd = [
		path_to_eclipse,
		'-application',
		'org.eclipse.jdt.core.JavaCodeFormatter',
		'-verbose',
		'-config',
		configfile,
		os.path.join(path, '*.java')
	]
	subprocess.call(cmd)
	
def rglob(rootdir, pattern='*'):
	for root, dirnames, filenames in os.walk(rootdir):
		for filename in fnmatch.filter(filenames, pattern):
			yield os.path.join(root, filename)

if __name__ == '__main__':
    srcdirs = ['src/main/java', 'src/test/java', 'src/it/java']
    srcdirs = [os.path.join(project_dir, x) for x in srcdirs]
    srcdirs = [x for x in srcdirs if os.path.isdir(x)]
    
    newheader = file('java_header.txt').read()
    
    for d in srcdirs:
        for j in rglob(d, '*.java'):
            convert_to_utf8(j)
            replace_java_header(j, newheader)
            if path_to_eclipse <> None:
                format_java_files('./etc/eclipse-formatter-config.xml', d)
            dos2unix(j)
             
    for xml in rglob(project_dir, '*.xml'):
        xmllint(xml)
        dos2unix(xml)
