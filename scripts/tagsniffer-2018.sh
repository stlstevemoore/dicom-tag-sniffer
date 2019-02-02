#!/bin/sh

cp ../tagsniffer-2018/out/artifacts/tagsniffer_2018_jar/tagsniffer-2018.jar .

java -cp tagsniffer-2018.jar edu/wustl/mir/erl/tagsniffer/TagsnifferCommandLine \
	./images ./output

