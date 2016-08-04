#!/bin/bash
JAR_FILE="$(pwd)/DivvyHost.jar"
LOG_FILE='/tmp/divvy.log'
PID_FILE='/tmp/divvy.pid'


function init {
	if ! type java &> /dev/null 
	then
		echo '`java` not found!';
		echo 'Make Sure `java` is Included in your $PATH';
		echo ;
		echo 'If java not Installed, then try';
		echo 'sudo apt-get install openjdk-8-jre';
		echo ;
		exit;
	fi;

	# Linux Destro, Assuming Python will be installed
}

# Check, if Everything is Alright
init;

function createAutoStart {
	
	filenameinit=$HOME/Divvy/init.sh;

	echo '#!/bin/bash' > filenameinit;
	echo 'pushd $1' >> filenameinit;
	echo 'bash bin.sh start' >> filenameinit;
	echo 'popd' >> filenameinit;

	chmod +x filenameinit || exit 1;

	filename=~/.config/autostart/Divvy.desktop;
	
	mkdir ~/.config;
	mkdir ~/.config/autostart;

	echo '[Desktop Entry]' > $filename;
	echo 'Type=Application' >> $filename;
	echo "Exec=$HOME/Divvy/init.sh $HOME/Divvy/" >> $filename;
	echo 'X-GNOME-Autostart-enabled=true' >> $filename;
	echo 'NoDisplay=false' >> $filename;
	echo 'Hidden=false' >> $filename;
	echo 'Name[en_IN]=Divvy' >> $filename;
	echo 'Comment[en_IN]=Divvy Host' >> $filename;
	echo 'X-GNOME-Autostart-Delay=0' >> $filename;

}

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

	createAutoStart;

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
		"restart" )
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