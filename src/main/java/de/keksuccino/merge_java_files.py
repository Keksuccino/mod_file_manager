import os

def merge_java_files(root_dir, output_file):
    # Open output file in write mode
    with open(output_file, 'w', encoding='utf-8') as outfile:
        # Walk through directory and subdirectories
        for dirpath, dirnames, filenames in os.walk(root_dir):
            # Filter for .java files
            java_files = [f for f in filenames if f.endswith('.java')]
            
            for java_file in java_files:
                # Get full file path
                file_path = os.path.join(dirpath, java_file)
                
                # Write file header
                outfile.write('='*50 + '\n')
                outfile.write(f'FILE: {file_path}\n')
                outfile.write('='*50 + '\n\n')
                
                # Read and write file content
                try:
                    with open(file_path, 'r', encoding='utf-8') as infile:
                        outfile.write(infile.read())
                    outfile.write('\n\n')
                except Exception as e:
                    outfile.write(f'Error reading file: {str(e)}\n\n')

if __name__ == '__main__':
    # Use current directory as root
    current_dir = os.getcwd()
    output_file = 'merged_java_files.txt'
    
    merge_java_files(current_dir, output_file)
    print(f'Files have been merged into {output_file}')