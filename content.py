#!/usr/bin/env python

import os
import click
    

packagename = "info.guardianproject.mrapp"     
    
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

    os.system("cd liger-content ; python generate_content.py")

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
    os.system('cd liger-content ; python pull_translations.py')

    click.echo("\n\ngenerating localized content...\n\n")
    os.system('cd liger-content ; python generate_localized_content.py ')
   
@cli.command()  
def zip_content():
    """this creates the zipped blob of content and copies it in to storymaker's assets folder as its .obb file"""

    os.system("cd liger-content/assets ; zip -n .mp4 -r zipped .")

@cli.command()  
def adb_push_obb():
    """adb push to /sdcard/Android/<package>/obb"""

    os.system("cd liger-content/assets ; adb push zipped.zip /sdcard/Android/obb/%s/main.1.%s.obb" % (packagename, packagename))
    
@cli.command()  
def adb_push_files():
    """adb push to /sdcard/Android/<package>/files"""

    os.system("cd liger-content/assets ; adb push zipped.zip /sdcard/Android/data/%s/files/main.1.%s.obb" % (packagename, packagename))
    
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


cli.add_command(clone)
cli.add_command(pull)
cli.add_command(push_strings)
cli.add_command(update_strings)
cli.add_command(zip_content)
cli.add_command(adb_push)
cli.add_command(adb_push_obb)
cli.add_command(adb_push_files)
cli.add_command(build_zip_push)

cli()
