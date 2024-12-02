import os
import shutil
import argparse

def copy_env_file_to_services(src_file, root_dir, env_value):
    if not os.path.isfile(src_file):
        print(f"Source file {src_file} does not exist.")
        return

    for subdir, dirs, files in os.walk(root_dir):
        if subdir.endswith('Service') or subdir.endswith('Objs'):
            dest_file = os.path.join(subdir, '.env')

            try:
                shutil.copy(src_file, dest_file)
                with open(dest_file, 'a') as file:
                    file.write(f"\nENV={env_value}\n")
                print(f"Copied .env to: {subdir}")
            except Exception as e:
                print(f"Error copying .env to {subdir}: {e}")

if __name__ == '__main__':
    src_env_file = './.env'
    root_directory = '.'
    
    parser = argparse.ArgumentParser(description="Copy .env file to services and append ENV variable.")
    parser.add_argument("--env", type=str, help="Value for the ENV variable", required=True)
    args = parser.parse_args()
    
    if args.env != "dev" and args.env != "prod":
        print(f"Please only use 'dev' or 'prod', not '{args.env}'")
        exit(-1)

    copy_env_file_to_services(src_env_file, root_directory, args.env)
