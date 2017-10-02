android_app: git_submodules libs/jcardsim-3.0.4-SNAPSHOT-android.jar
	android update project --path .
	ant debug

git_submodules:
	git submodule init
	git submodule update

libs/jcardsim-3.0.4-SNAPSHOT-android.jar:
	cd jcardsim;\
	mvn initialize;\
	mvn clean install;\
	cp target/jcardsim-3.0.4-SNAPSHOT-android.jar ../libs/

install:
	adb install -r bin/UserControlledDecryptionOperations-debug.apk
