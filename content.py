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

@cli.command()
def generate_json():
    """generate json from yaml, also splits out strings into translation intermediates ready for pushing"""

    os.system("cd liger-content ; python generate_content.py ; python generate_localized_content.py")

@cli.command()
def push_strings():
    """First generates the content to extract the latest source strings, then pushes them to transifex"""

    click.echo("\n\nupdateing content...\n\n")
    os.system("cd liger-content ; python generate_content.py")

    click.echo("\n\npushing strings to transifex\n\n")
    os.system('cd liger-content ; tx push -s')

@cli.command()
def update_strings():
    """pull down translated strings from transifex and generate localized json"""

    click.echo("\n\npulling translations from transifex...\n\n")
    os.system('cd liger-content ; python pull-translations.py')

    click.echo("\n\ngenerating localized content...\n\n")
    os.system('cd liger-content ; python generate_localized_content.py ')

@cli.command()
def zip_content():
    """this creates the zipped blob of content and copies it in to storymaker's assets folder as its .obb file"""

    if query_yes_no("zip beta paths?"):
        os.system("rm liger-content/assets/beta.main.3.obb ; cd liger-content/assets ; zip -n .mp4 -r ../zips/beta.main.3.obb org.storymaker.app/beta")
        print "content generated at: liger-content/zips/beta.main.3.obb"
    
    if query_yes_no("zip main?"):
        os.system("rm liger-content/assets/main.1044.org.storymaker.app.obb ; cd liger-content/assets ; zip -n .mp4 -r main.1044.org.storymaker.app.obb org.storymaker.app/default")
        print "content generated at: liger-content/assets/main.1044.org.storymaker.app.obb"
    
    if query_yes_no("zip learning guide?"):
        os.system("rm liger-content/assets/learning_guide.main.2.obb; cd liger-content/assets ; zip -n .mp4 -r learning_guide.main.2.obb org.storymaker.app/learning_guide")
        print "content generated at: liger-content/assets/learning_guide.main.2.obb"
        
    if query_yes_no("zip burundi?"):
        os.system("rm liger-content/assets/burundi.main.4.obb; cd liger-content/assets ; zip -n .mp4 -r burundi.main.4.obb org.storymaker.app/burundi")
        print "content generated at: liger-content/assets/burundi.main.4.obb"
        
    if query_yes_no("zip burundi patch?"):
        os.system("rm liger-content/assets/burundi.patch.4.obb; cd liger-content/assets/patch/ ; zip -n .mp4 -r burundi.patch.4.obb org.storymaker.app/burundi ; mv burundi.patch.4.obb ..")
        print "content generated at: liger-content/assets/burundi.patch.4.obb"
        
    if query_yes_no("zip persian?"):
        os.system("rm liger-content/assets/persian.main.4.obb; cd liger-content/assets ; zip -n .mp4 -r persian.main.4.obb org.storymaker.app/persian")
        print "content generated at: liger-content/assets/persian.main.4.obb"

    if query_yes_no("zip mena?"):
        os.system("rm liger-content/assets/mena.main.4.obb; cd liger-content/assets ; zip -n .mp4 -r mena.main.4.obb org.storymaker.app/mena")
        print "content generated at: liger-content/assets/mena.main.4.obb"
        
    if query_yes_no("zip IJF15?"):
        os.system("rm liger-content/assets/ijf15.main.4.obb; cd liger-content/assets ; zip -n .mp4 -r ../zips/ijf15.main.4.obb org.storymaker.app/ijf15")
        print "content generated at: liger-content/zips/ijf15.main.4.obb"
        
    #if query_yes_no("zip dressgate?"):
    #    os.system("rm liger-content/assets/dressgate.main.1.obb; cd liger-content/assets ; zip -n .mp4 -r dressgate.main.1.obb org.storymaker.app/dressgate")
    #    print "content generated at: liger-content/assets/dressgate.main.1.obb"
    
    os.system("sha256sum liger-content/zips/*.obb")
    os.system("ls -l liger-content/zips/*.obb")
    
@cli.command()
def adb_push_obb():
    """adb push to /sdcard/Android/<package>/obb"""
    os.system("cd liger-content/assets ; adb push zipped.zip /sdcard/Android/obb/%s/main.1.%s.obb" % (packagename, packagename))

@cli.command()
def adb_push_files():
    """adb push to /sdcard/Android/<package>/files"""

    if query_yes_no("adb push beta.main.3.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push beta.main.3.obb /sdcard/Android/data/%s/files/beta.main.3.obb" % (packagename))
    if query_yes_no("adb push learning_guide.main.2.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push learning_guide.main.2.obb /sdcard/Android/data/%s/files/learning_guide.main.2.obb" % (packagename))
    if query_yes_no("adb push learning_guide.patch.2.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push learning_guide.patch.2.obb /sdcard/Android/data/%s/files/learning_guide.patch.2.obb" % (packagename))
        
    if query_yes_no("adb push burundi.main.4.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push burundi.main.4.obb /sdcard/Android/data/%s/files/burundi.main.4.obb" % (packagename))
    if query_yes_no("adb push burundi.patch.4.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push burundi.patch.4.obb /sdcard/Android/data/%s/files/burundi.patch.4.obb" % (packagename))
        
    if query_yes_no("adb push mena.main.4.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push mena.main.4.obb /sdcard/Android/data/%s/files/mena.main.4.obb" % (packagename))
        
    if query_yes_no("adb push persian.main.4.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push persian.main.4.obb /sdcard/Android/data/%s/files/persian.main.4.obb" % (packagename))
        
    if query_yes_no("adb push main.1044.org.storymaker.app.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push main.1044.org.storymaker.app.obb /sdcard/Android/data/%s/files/main.1044.org.storymaker.app.obb" % (packagename))
    if query_yes_no("adb push patch.1044.org.storymaker.app.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push patch.1044.org.storymaker.app.obb /sdcard/Android/data/%s/files/patch.1044.org.storymaker.app.obb" % (packagename))
        
    if query_yes_no("adb push ijf15.main.4.obb to device files/ folder?"):
        os.system("cd liger-content/zips ; adb push ijf15.main.4.obb /sdcard/Android/data/%s/files/ijf15.main.4.obb" % (packagename))
        
    #if query_yes_no("adb push dressgate.main.1.obb to device files/ folder?"):
    #    os.system("cd liger-content/assets ; adb push dressgate.main.1.obb /sdcard/Android/data/%s/files/dressgate.main.1.obb" % (packagename))

@cli.command()
def adb_push():
    """adb push to /sdcard/Android/<package>/files"""
    adb_push_files()

@cli.command()
def build_zip_push():
    """build the json, zip it, push it to sd"""

    generate_json()
    zip_content()
    adb_push()

@cli.command()
def build_zip_push_ijf15():
    """build the json, zip it, push it to sd"""

    os.system("cd liger-content ; python generate_content.py ; python generate_localized_content.py")
    os.system("rm liger-content/zips/ijf15.main.4.obb; cd liger-content/assets ; zip -n .mp4 -r ../zips/ijf15.main.4.obb org.storymaker.app/ijf15")
    print "content generated at: liger-content/zips/ijf15.main.4.obb"
    os.system("cd liger-content/zips ; adb push ijf15.main.4.obb /sdcard/Android/data/%s/files/ijf15.main.4.obb" % (packagename))
    os.system("sha256sum liger-content/zips/ijf15.main.4.obb")
    os.system("ls -l liger-content/zips/ijf15.main.4.obb")
    adb_push()

cli.add_command(clone)
cli.add_command(pull)
cli.add_command(push_strings)
cli.add_command(update_strings)
cli.add_command(zip_content)
cli.add_command(adb_push)
cli.add_command(adb_push_obb)
cli.add_command(adb_push_files)
cli.add_command(build_zip_push)
cli.add_command(build_zip_push_ijf15)

cli()
