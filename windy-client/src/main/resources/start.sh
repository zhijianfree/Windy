port=$1
if [ -z $port ]; then
	echo "no port config not close process"
else
	name=$(lsof -i:$port | tail -1 | awk '"$1"!=""{print $2}')
	if [ -z $name ]; then
		echo "No process can be used to killed!"
	else
		id=$(lsof -i:$port | tail -1 | awk '"$1"!=""{print $2}')
		kill -9 $id
		echo "Process name=$name($id) kill!"
	fi
fi