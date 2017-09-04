#!/bin/bash
JAR_FILE="DivvyHost.jar"
LOG_FILE='/tmp/divvy.log'
PID_FILE='/tmp/divvy.pid'
: ;BASEDIR=$(dirname "$0");

# Just Make Executable if not
chmod +x $0 > /dev/null;

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
	filename=~/.config/autostart/Divvy.desktop;
	
	mkdir -p ~/.config;
	mkdir -p ~/.config/autostart;

	echo '[Desktop Entry]' > $filename;
	echo 'Type=Application' >> $filename;
	echo "Exec=$HOME/Divvy/bin.sh start" >> $filename;
	echo 'X-GNOME-Autostart-enabled=true' >> $filename;
	echo 'NoDisplay=false' >> $filename;
	echo 'Hidden=false' >> $filename;
	echo 'Name[en_IN]=Divvy' >> $filename;
	echo 'Comment[en_IN]=Divvy Host' >> $filename;
	echo 'X-GNOME-Autostart-Delay=0' >> $filename;

	# For Easy Access
	echo ;
	echo "To add it in /usr/bin, please use ";
	echo "Use \`sudo ln -s $HOME/Divvy/bin.sh /usr/bin/divvy\` ";
	echo ;


}

# Print Usage
function _help {
	echo "Usage: bin.sh <option>"
	echo "Usage: bin.sh install"
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
	echo -e " restart\tRestart DivvyHost"
	echo
	exit
}

# Start DivvyHost
function start {
	pushd $BASEDIR;
	nohup java -jar "$JAR_FILE" -nogui &> $LOG_FILE & echo $! > $PID_FILE;
	popd
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
	sed "s|^: ;BASEDIR.*|: ;BASEDIR\=$HOME/Divvy|g" < "$BASEDIR/bin.sh" > "$HOME/Divvy/bin.sh"
	chmod +x "$HOME/Divvy/bin.sh"

	cp -r "$BASEDIR/Conf" "$HOME/Divvy/" 2> /dev/null
	cp "$BASEDIR/$JAR_FILE" "$HOME/Divvy/DivvyHost.jar"

	createAutoStart;

	exit 0
}


function launch {
	pushd $BASEDIR;
	java -jar "$JAR_FILE" $1;
	popd;
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
			launch;
			;;
		"-nogui" )
			launch -nogui;
			;;
		"-test" )
			launch -service=test;
			;;
		"-show" )
			launch -service=startgui;
			;;
		"-hide" )
			launch -service=stopgui;
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
