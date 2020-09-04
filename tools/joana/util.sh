#! /bin/sh
set -e

correct_java_version(){
	if ! (java -version 2>&1 >/dev/null | grep "1.8" > /dev/null) ; then 
  	echo "Java version is not 1.8, trying to use java8 if installed"
 		lib=`update-java-alternatives -l | grep "[/].*1\.8.*" --only-matching`
		if [ $lib == "" ] ; then
			$lib="/usr/lib/jvm/java-1.8.0-openjdk-amd64"
    fi
    export PATH="$lib/bin:$PATH"
  fi
}

[ -z "$JOANA_UNINITIALIZED" ] && export JOANA_UNINITIALIZED=".*"