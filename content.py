#!/usr/bin/env python

import os
import click
import sys

def query_yes_no(question, default="yes"):
    """Ask a yes/no question via raw_input() and return their answer.

    "question" is a string that is presented to the user.
    "default" is the presumed answer if the user just hits <Enter>.
        It must be "yes" (the default), "no" or None (meaning
        an answer is required of the user).

    The "answer" return value is True for "yes" or False for "no".
    """
    valid = {"yes": True, "y": True, "ye": True,
             "no": False, "n": False}
    if default is None:
        prompt = " [y/n] "
    elif default == "yes":
        prompt = " [Y/n] "
    elif default == "no":
        prompt = " [y/N] "
    else:
        raise ValueError("invalid default answer: '%s'" % default)

    while True:
        sys.stdout.write(question + prompt)
        choice = raw_input().lower()
        if default is not None and choice == '':
            return valid[default]
        elif choice in valid:
            return valid[choice]
        else:
            sys.stdout.write("Please respond with 'yes' or 'no' "
                             "(or 'y' or 'n').\n")

packagename = "org.storymaker.app"

@click.group()
def cli():
    pass

@cli.command()
def clone():
    """clone the liger content git repo"""

    os.system('git clone https://github.com/scalio/liger-content.git')

@cli.command()
def pull():
    """update the liger content repo"""

    os.system('cd liger-content ; git pull')

def _generate_json():
    os.system("cd liger-content ; python generate_content.py ; python generate_localized_content.py")

@cli.command()
def generate():
    """generate json from yaml, also splits out strings into translation intermediates ready for pushing"""
    _generate_json()

@cli.command()
def push_strings():
    """First generates the content to extract the latest source strings, then pushes them to transifex"""

    click.echo("\n\nupdateing content...\n\n")
    _generate_json()

    click.echo("\n\npushing strings to transifex\n\n")
    os.system('cd liger-content ; tx push -s')

@cli.command()
def update_strings():
    """pull down translated strings from transifex and generate localized json"""

    click.echo("\n\npulling translations from transifex...\n\n")
    os.system('cd liger-content ; python pull-translations.py')

    click.echo("\n\ngenerating localized content...\n\n")
    os.system('cd liger-content ; python generate_localized_content.py ')

def zip_pack(pack, version):
    print "content generated at: liger-content/zips/{0}.main.{1}.obb".format(pack, version)
    os.system("rm liger-content/assets/{0}.main.{1}.obb ; cd liger-content/assets ; zip -n .mp4:.ogg:.mov:.qt:.wav:.au:.aiff:.3gp:.avi -r ../zips/{0}.main.{1}.obb org.storymaker.app/{0}".format(pack, version))
    print("")
    os.system("sha256sum liger-content/zips/{0}.main.{1}.obb".format(pack, version))
    os.system("ls -l liger-content/zips/{0}.main.{1}.obb".format(pack, version))
    print("")
    
@cli.command()
@click.argument('pack')
@click.argument('version')
def zip_one_content_pack(pack, version):
    """this creates the zipped blob of content and copies it in to storymaker's assets folder as its .obb file"""    
    zip_pack(pack, version)

@cli.command()
def zip_content():
    """this creates the zipped blob of content and copies it in to storymaker's assets folder as its .obb file"""
    
    if query_yes_no("zip beta paths?"):
        zip_pack('beta', '5')
        
    if query_yes_no("zip mobile_photo_101?"):
        zip_pack("mobile_photo_101", "2")
    
    if query_yes_no("zip main?"):
        os.system("rm liger-content/assets/main.1044.org.storymaker.app.obb ; cd liger-content/assets ; zip -n .mp4:.ogg:.mov:.qt:.wav:.au:.aiff:.3gp:.avi -r main.1044.org.storymaker.app.obb org.storymaker.app/default")
        print "content generated at: liger-content/assets/main.1044.org.storymaker.app.obb"
    
    if query_yes_no("zip learning guide?"):
        zip_pack("learning_guide", "2")
        
    if query_yes_no("zip burundi?"):
        zip_pack("burundi", "4")
        
    if query_yes_no("zip burundi patch?"):
        os.system("rm liger-content/assets/burundi.patch.4.obb; cd liger-content/assets/patch/ ; zip -n .mp4:.ogg:.mov:.qt:.wav:.au:.aiff:.3gp:.avi -r burundi.patch.4.obb org.storymaker.app/burundi ; mv burundi.patch.4.obb ..")
        print "content generated at: liger-content/assets/burundi.patch.4.obb"
        
    if query_yes_no("zip persian?"):
        zip_pack("persian", "4")

    if query_yes_no("zip mena?"):
        zip_pack("mena", "4")
        
    if query_yes_no("zip IJF15?"):
        zip_pack("IJF15", "4")
        
    #if query_yes_no("zip dressgate?"):
    #    os.system("rm liger-content/assets/dressgate.main.1.obb; cd liger-content/assets ; zip -n .mp4:.ogg:.mov:.qt:.wav:.au:.aiff:.3gp:.avi -r dressgate.main.1.obb org.storymaker.app/dressgate")
    #    print "content generated at: liger-content/assets/dressgate.main.1.obb"
    
@cli.command()
def adb_push_obb():
    """adb push to /sdcard/Android/<package>/obb"""
    os.system("cd liger-content/assets ; adb push zipped.zip /sdcard/Android/obb/%s/main.1.%s.obb" % (packagename, packagename))

def push_obb_file(pack, version):
    os.system("cd liger-content/zips ; adb push {1}.main.{2}.obb /sdcard/Android/data/{0}/files/{1}.main.{2}.obb".format(packagename, pack, version))
    
@cli.command()
def adb_push_files():
    """adb push to /sdcard/Android/<package>/files"""

    if query_yes_no("adb push beta.main.5.obb to device files/ folder?"):
        push_obb_file("beta", "5")

    if query_yes_no("adb push mobile_photo_101.main.2.obb to device files/ folder?"):
        push_obb_file("mobile_photo_101", "2")
        
    if query_yes_no("adb push learning_guide.main.2.obb to device files/ folder?"):
        push_obb_file("learning_guide", "2")
    if query_yes_no("adb push learning_guide.patch.2.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push learning_guide.patch.2.obb /sdcard/Android/data/%s/files/learning_guide.patch.2.obb" % (packagename))
        
    if query_yes_no("adb push burundi.main.4.obb to device files/ folder?"):
        push_obb_file("burundi", "4")
        os.system("cd liger-content/zips ; adb push burundi.main.4.obb /sdcard/Android/data/%s/files/burundi.main.4.obb" % (packagename))
    if query_yes_no("adb push burundi.patch.4.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push burundi.patch.4.obb /sdcard/Android/data/%s/files/burundi.patch.4.obb" % (packagename))
        
    if query_yes_no("adb push mena.main.4.obb to device files/ folder?"):
        push_obb_file("mena", "4")
        
    if query_yes_no("adb push persian.main.4.obb to device files/ folder?"):
        push_obb_file("persian", "4")
        
    if query_yes_no("adb push main.1044.org.storymaker.app.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push main.1044.org.storymaker.app.obb /sdcard/Android/data/%s/files/main.1044.org.storymaker.app.obb" % (packagename))
    if query_yes_no("adb push patch.1044.org.storymaker.app.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push patch.1044.org.storymaker.app.obb /sdcard/Android/data/%s/files/patch.1044.org.storymaker.app.obb" % (packagename))
        
    if query_yes_no("adb push ijf15.main.4.obb to device files/ folder?"):
        push_obb_file("IJF15", "4")

@cli.command()
def adb_push():
    """adb push to /sdcard/Android/<package>/files"""
    adb_push_obb_file()

@cli.command()
def build_zip_push():
    """build the json, zip it, push it to sd"""

    generate_json()
    zip_content()
    adb_push()

@cli.command()
def build_zip_push_mobile_photo_101():
    """build the json, zip it, push it to sd"""

    generate_json()
    os.system("rm liger-content/zips/mobile_photo_101.main.2.obb; cd liger-content/assets ; zip -n .mp4 -r ../zips/mobile_photo_101.main.2.obb org.storymaker.app/mobile_photo_101")
    print "content generated at: liger-content/zips/mobile_photo_101.main.2.obb"
    os.system("cd liger-content/zips ; adb push mobile_photo_101.main.2.obb /sdcard/Android/data/%s/files/mobile_photo_101.main.2.obb" % (packagename))
    os.system("sha256sum liger-content/zips/mobile_photo_101.main.2.obb")
    os.system("ls -l liger-content/zips/mobile_photo_101.main.2.obb")
    adb_push()

def push_test_indexes(pack, version, avail_index_version):
    file_size = 12345
    checksum = "abc123"
    template = """[{{
        "expansionId": "{1}",
        "patchOrder": "4",
        "title": "Testing",
        "packageName": "{0}",
        "description": "testing",
        "thumbnailPath": "images/content_packs/{1}_cover.png",
        "expansionFilePath": "Android/data/{0}/files/",
        "expansionFileUrl": "https://s3-us-west-1.amazonaws.com/storymakerorg/appdata/obb/{0}/",
        "expansionFileVersion": "{2}",
        "expansionFileSize": "{3}",
        "expansionFileChecksum": "{4}"
    }}]""".format(packagename, pack, version, file_size, checksum)
    with open('tmp.json', 'w') as f:
        f.write(template)
        f.close()
    os.system("adb push tmp.json /sdcard/Android/data/{0}/files/available_index.{1}.json".format(packagename, avail_index_version))
    os.system("adb push tmp.json /sdcard/Android/data/{0}/files/installed_index.json".format(packagename))

@cli.command()   
@click.argument('pack')
@click.argument('version') 
@click.argument('avail_index_version') 
def push_test_files(pack, version, avail_index_version):
    push_test_indexes(pack, version, avail_index_version)
    push_obb_file(pack, version)
    
    
cli.add_command(clone)
cli.add_command(pull)
cli.add_command(push_strings)
cli.add_command(update_strings)
cli.add_command(zip_content)
cli.add_command(zip_one_content_pack)
cli.add_command(adb_push)
cli.add_command(adb_push_obb)
cli.add_command(adb_push_files)
cli.add_command(generate)
cli.add_command(build_zip_push)
cli.add_command(build_zip_push_mobile_photo_101)
cli.add_command(push_test_files)

cli()
