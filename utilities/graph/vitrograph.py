#!/usr/bin/env python

import os
import sys
import commands
from sets import Set

### properties
#databae to work from
g_db = 'vivo3'
g_passwd = 'RedRed'
g_user = 'root'

#depth of graph to draw
g_depth = 2

# these are the formats for the dot file:
g_edge_format  = '  %s -> %s;\n'
g_label_format = '  %s [href="%s", label="%s"];\n'

#directories
g_workDir = 'graphdata'
g_imageDir = g_workDir+'/images'
g_dotDir = g_workDir + '/dot'
g_mapDir = g_workDir + '/maps'

#commands passed to system
g_dotImg_cmd = 'dot -Tpng %s -o %s' # (dotFile, outFile)
g_dotMap_cmd = 'dot -Tcmap %s -o %s' # (dotFile, outFile)

#header to dot graph
g_dot_header = '  size="9,20";\n  ratio=compress;' + \
               '  rankdir=LR; \n '

### system functions
def pipeFromMysql( query ):
    "execute a mysql query and get the results back as list of rows"
    cmd = 'echo "%s" | mysql -u%s -p%s --skip-column-names %s' % (query, g_user, g_passwd, g_db)
    failure, res = commands.getstatusoutput( cmd )
    if failure: 
        print 'could not run query:', query
        print 'command: ', cmd
        
    resList = [ ]
    for line in res.split('\n'):
        if line == '': continue
        resList.append(line)        
    return resList  

def setupDirs():
    dirs = [g_workDir,g_imageDir,g_dotDir,g_mapDir]
    for dir in dirs:
        cmd = 'mkdir -p ' + dir
        failure = os.system(cmd)
        if failure:
            print 'command failed: %s' % (cmd)
            break

### SQL commands

#sql queries should be here
g_classId_query = "SELECT id from vclass;"
g_subclass_query = "SELECT superclassid, subclassid " + \
        "FROM classes2classes where superclassid=%s"
g_superclass_query = 'SELECT superclassid, subclassid ' +\
    'FROM classes2classes WHERE subclassid=%s'

def getClassIds():
    return pipeFromMysql( g_classId_query )
      
def getSubClasses(classId, depth, visited=None):    
    """Returns subclasses as a list of ['superId', 'subId'].
    
    depth of 0 get no rows, depth of 1 gets children, depth of 2 gets
    children and grandchildren, etc."""
    if visited == None: visited = Set([]) #always clear visited    
    visited.add(classId)
    
    res = [ ]
    if depth > 0:
        query =  g_subclass_query % (str(classId))
        rows = pipeFromMysql( query )
        for row in rows:
            res.append( row.split('\t') )

    children=[]
    if depth > 1:
        for pair in res:
            child = int(pair[1])
            if child in visited: continue
            visited.add( child )
            children = children + getSubClasses(child,depth-1,visited) 
    return res + children

def getSuperClasses( classId ):
    query = g_superclass_query % (str(classId))
    rows = pipeFromMysql(query)
    res = []
    for row in rows:
        res.append( row.split('\t') )
    return res

def queryForClassNames( ids ):
    "given a sequence of ids return a dict of id:'classname'"
    if len(ids) == 0 : return {}
    where='WHERE id in (' + reduce(lambda x,y:x+','+y, ids) + ')'
    query = 'SELECT id, name FROM vclass ' + where
    rows = pipeFromMysql(query)
    ret = {}
    for row in rows:
        print row
        id,name = row.split('\t')
        ret[id]=name
    return ret

### Dot output generation 
def makeClassHierDot(classId):
    "make a dot graph string for a given class"
    graph, labels = '',''
    subs = getSubClasses(classId, g_depth )
    supers = getSuperClasses( classId )
    graph = lists2DotEdges( supers ) + lists2DotEdges( subs )
    #same = list2DotRankSame( supers + [[classId, classId]] )
    labels = list2DotLables( supers + subs )
    return 'digraph G{\n%s\n%s\n%s}' % (g_dot_header,graph,labels)

def list2DotRankSame( pairs ):
    ret=''
    classSet = Set([])
    for pair in pairs:
        classSet.add(pair[0])
        classSet.add(pair[1])
    for classid in classSet:
        ret = ret +  classid + '; '
    return '{ rank = same ; ' + ret + '}'

def lists2DotEdges( pairs ):
    "given a set of [classid,classid] pairs return a dot graph edge string"
    ret = ''
    for pair in pairs:
        if pair[0] == '' or pair[1] == '': continue
        ret = ret + g_edge_format % (pair[0],pair[1])
    return ret

def list2DotLables( pairs ):
    "given [classid, classid] pairs return a dot label string"
    classSet = Set([])
    for pair in pairs:
        classSet.add(pair[0])
        classSet.add(pair[1])
    id2name = queryForClassNames( classSet )
    ret = ''
    for classid in id2name:
        name = id2name[classid]
        href = makeClassHierHtml(classid)
        ret = ret + g_label_format % (classid,href,name)
    return ret

def makeClassHierHtml(classId):
    return "classHier.jsp?classid=%s" % (str(classId))

### File building commands
def writeClassHierDot(classId):
    graph = makeClassHierDot(classId)
    filename = g_dotDir+'/'+str(classId)+'.dot'
    try:
        fout = open(filename, 'w')
        try:
            fout.write(graph)
        finally:
            fout.close()
    except IOError:
        print 'unable to open file ' + filename + 'for writing'

def makeClassHierImage(classId):
    dotFile = g_dotDir+'/'+str(classId)+'.dot'
    imgOut = g_imageDir+'/'+str(classId)+'.png'
    cmd = g_dotImg_cmd % (dotFile, imgOut)
    os.system(cmd)
    
def makeClassHierMap(classId):
    dotFile = g_dotDir+'/'+str(classId)+'.dot'
    out = g_mapDir+'/'+str(classId)+'.map'
    cmd = g_dotMap_cmd % (dotFile, out)
    os.system(cmd) 

def doGraphGeneration( ):
    "Make graph dot files, images and imagemaps"
    classIds = getClassIds()
    print classIds
    for classId in classIds:
        print "doing class " + classId,
        writeClassHierDot( classId )
        print '.',
        makeClassHierImage( classId )
        print '.',
        makeClassHierMap( classId )
        print '.'
        

