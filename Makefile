
all: android_app encrypting_cloud_storages

android_app:
	cd Android ;\
	./gradlew clean build

encrypting_cloud_storages:
	git submodule init
	git submodule update
	cd encrypting-cloud-storages/build/ ;\
	cmake .. ;\
	make install
