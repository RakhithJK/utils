#!/bin/bash

echo -e "\n\n"

wiki_root=$1

if [ ! -d $wiki_root ];
then
	echo -e "  --->  缺少参数: wiki根路径\n"
	exit 1
fi

cd $wiki_root
echo -e "  --->  进入wiki路径目录：$wiki_root\n"

if [ $# -gt 1 ];
then
	echo -e "  --->  开始清理\n"
	params=($*)
	paramsNum=$#
	for ((i=1;i<$paramsNum;i++))
	do
		param=${params[$i]}
		echo -e "  --->  开始处理参数：$param"
		if [ -f $param ];
		then
			git rm -rf $param
			echo -e "  --->  执行git删除文件"
			touch $param
			echo -e "  --->  创建新文件"
		elif [ -d $param ];
		then
			git rm -rf $param/*
			echo -e "  --->  执行git删除文件夹"
			mkdir -p $param
			echo -e "  --->  创建新文件夹"
		else
			echo -e "  --->  不是文件也不是文件夹：$param"
		fi
		echo -e "  --->  参数处理完毕：$param\n"
	done;
fi

git add *
echo -e "  --->  添加版本控制\n"

echo -e "\n"
