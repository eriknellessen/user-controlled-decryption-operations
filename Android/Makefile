
android_app:
	git submodule init
	git submodule update
#svn checkout svn://svn.code.sf.net/p/javacardopenpgp/code/trunk javacardopenpgp-code
	android update project --path .
	ant debug

install:
	adb install -r bin/UserControlledDecryptionOperations-debug.apk