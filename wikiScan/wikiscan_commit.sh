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

git add *
echo -e "  --->  添加版本控制\n"

git commit --amend --no-edit --allow-empty
echo -e "  --->  提交完毕\n"

echo -e "\n"
