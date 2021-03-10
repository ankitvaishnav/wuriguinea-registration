reg_target = "./registration/registration-client/target"
out_put_dir = "guinean-reg-client"
zip_generated_name = "mosip-sw-1.1.1.zip"


all:  global_build make_output_dir copy_necessary_files unzip_app copy_libs zip_the_final_directory

global_build:
	cd registration && mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true

#global_build:
#	mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true

make_output_dir:
	rm -rf $(out_put_dir) || true
	mkdir $(out_put_dir)

copy_necessary_files:
	cd $(out_put_dir) && rm -rf *
	cp $(reg_target)/$(zip_generated_name) $(out_put_dir)
	cp $(reg_target)/MANIFEST.MF $(out_put_dir)

unzip_app:
	cd $(out_put_dir) && unzip $(zip_generated_name) && rm $(zip_generated_name)

copy_libs:
	cp $(reg_target)/lib/* $(out_put_dir)/lib
	cd $(out_put_dir)/lib && mv mosip-client.jar ../bin && mv mosip-services.jar ../bin

zip_the_final_directory:
	rm guinea-reg-client.zip || true
	zip -r guinea-reg-client guinean-reg-client
	rm -r guinean-reg-client || true
