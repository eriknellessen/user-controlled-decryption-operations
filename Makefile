
encrypting_cloud_storages:
	git submodule init
	git submodule update
	cd encrypting-cloud-storages/fuseecs/ ;\
	./configuration.sh ;\
	cd -
	make -C encrypting-cloud-storages/fuseecs
	make -C encrypting-cloud-storages/share_a_folder

android_app:
	make -C Android