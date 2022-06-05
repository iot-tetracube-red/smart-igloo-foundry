import glob
import os
import shutil
import sys


if len(sys.argv) > 2:
    print('You have specified too many arguments')
    sys.exit()

if len(sys.argv) < 2:
    print('You need to specify the base path where put the database\'s data files')
    sys.exit()

input_path = sys.argv[1]

if not os.path.isdir(input_path):
    print('The path specified does not exist')
    sys.exit()

base_db_data_path = input_path + os.path.sep + 'smart_igloo_db_data'
print('Let\'s create database base path ' + base_db_data_path)

isExist = os.path.exists(base_db_data_path)

if isExist:
  print('Warning: the base database path already exists, you should check it\'s content and eventually remove by hand')
  print('Exiting from procedure')
  sys.exit()

os.makedirs(base_db_data_path)
print('The new directory is created!')

db_scripts_path = base_db_data_path + os.path.sep + 'docker-entrypoint-initdb.d'
os.makedirs(db_scripts_path)

db_data_path = base_db_data_path + os.path.sep + 'db_data'
os.makedirs(db_data_path)

print('Copying SQL scripts into destination folder')
original_sql_scripts = os.path.dirname(os.path.realpath(__file__)) + os.path.sep + 'sql' + os.path.sep
for file in glob.glob(original_sql_scripts  + '*.sql'):
    print(file)
    shutil.copy(file, db_scripts_path)