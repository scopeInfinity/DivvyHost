#!/bin/bash
JAR_FILE="$(pwd)/DivvyHost.jar"
LOG_FILE='/tmp/divvy.log'
PID_FILE='/tmp/divvy.pid'

# Print Usage
function _help {
	echo "Usage: bin.sh <option> [DivvyHost.jar]"
	echo "Usage: bin.sh install [DivvyHost.jar]"
	echo
	echo "Options:"
	echo -e " -help\t\tHelp"
	echo -e " -run\t\tRun With GUI"
	echo -e " -nogui\t\tRun Without GUI"
	echo -e " -test\t\tCheck Any Running Instance"
	echo -e " -show\t\tShow GUI"
	echo -e " -hide\t\tHide GUI"
	echo
	echo "Service Options:"
	echo -e " start\t\tStart DivvyHost"
	echo -e " stop\t\tStop DivvyHost"
	echo -e " reload\t\tReload DivvyHost"
	echo
	exit
}

# Set jar Filename
if [ $# -eq 2 ]
then
	JAR_FILE=$2
fi

# Start DivvyHost
function start {
	nohup java -jar "$JAR_FILE" -nogui &> $LOG_FILE &
	echo $! > $PID_FILE;
	echo "DivvyHost Started Called"	
}

# Stop DivvyHost
function stop {
	if [ -f $PID_FILE ]
	then
		cat $PID_FILE | xargs kill;
		echo "DivvyHost Stop Called"
	else
		echo "No DivvyHost Last Instance"
	fi
}

# Install
function install {
	# Need to run at boot time

	echo "Installing";
	if mkdir "$HOME/Divvy/" &> /dev/null
	then
	echo "Directory Created $HOME/Divvy/"
	fi
	cp "bin.sh" "$HOME/Divvy/" 
	parent_dir="$(dirname "$JAR_FILE")"
	cp -r "lib/" "$HOME/Divvy/"
	cp "$JAR_FILE" "$HOME/Divvy/DivvyHost.jar"
	exit 0
}



if [ $# -eq 0 ]
then
	# If no Arguments
	_help
else
	# Check Given Option
	case $1 in
		"-help" )
			_help;
			;;
		"-run" )
			java -jar "$JAR_FILE";
			;;
		"-nogui" )
			java -jar "$JAR_FILE" -nogui;
			;;
		"-test" )
			java -jar "$JAR_FILE" -service=test;
			;;
		"-show" )
			java -jar "$JAR_FILE" -service=startgui;
			;;
		"-hide" )
			java -jar "$JAR_FILE" -service=stopgui;
			;;
		"start" )
			start;
			;;
		"stop" )
			stop;
			;;
		"reload" )
			stop;
			start;
			;;
		"install" )
			install;
			;;
		* )
			echo "Invalid Option!!"
			echo
			_help
			;;
	esac
fi