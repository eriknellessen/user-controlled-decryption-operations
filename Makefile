
all: android_app encrypting_cloud_storages

android_app:
	make -C Android

encrypting_cloud_storages:
	git submodule init
	git submodule update
	cd encrypting-cloud-storages/build/ ;\
	cmake .. ;\
	make install