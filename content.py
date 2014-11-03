#!/usr/bin/env python

import os
import click
    
@click.group()
def cli():
    pass

    
@cli.command()    
def setup(foo):
    os.system('git clone https://github.com/scalio/liger-content.git')

@cli.command()    
def update():
    os.system('cd liger-content ; git pull')
    
@cli.command()    
def push_strings():
    pass
    
@cli.command()    
def update_translations(foo):
    os.system('cd liger-content ; tx pull')
    os.system('cd liger-content ; python generate_localized_content.py ')
   
@cli.command()  
def generate_content():
    os.system("mkdir liger-content/assets ; cd liger-content/assets ; zip -n .mp4 -r zipped . ; mv zipped.zip ../../app/assets/main.1.obb")


@cli.command()
def build():
    print "building content zip..."



cli.add_command(setup)
cli.add_command(update)
cli.add_command(build)
cli.add_command(update_translations)
cli.add_command(generate_content)

cli()
