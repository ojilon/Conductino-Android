#!/usr/bin/env python3
"""
Project Mapper - Consolidates project files into a single text file for LLM context
Removes comments, unnecessary whitespace, and collects all code/config files
"""

import os
import re
from pathlib import Path
from datetime import datetime

# Configuration
OUTPUT_FILE = "project_context.txt"
EXCLUDE_DIRS = {
    '.git', '.gradle', 'build', 'dist', '.idea', '__pycache__', 
    'node_modules', '.venv', 'venv', '.env', 'target', '.DS_Store',
    '.firebase', '.github', '.circleci'
}
EXCLUDE_FILES = {
    '.gitignore', '.gitattributes', 'gradlew', 'gradlew.bat'
}
INCLUDE_EXTENSIONS = {
    # Java/Android
    '.java', '.kt', '.gradle', '.xml', '.properties',
    # Web
    '.html', '.css', '.js', '.json',
    # C/C++
    '.c', '.h', '.cpp', '.hpp', '.cc',
    # Build/Config
    '.cmake', '.cmake.in', '.sh', '.bat', '.gradle',
    # Documentation
    '.md', '.txt', '.yml', '.yaml',
    # Other
    '.pro', '.qmake'
}

def remove_java_comments(content):
    """Remove Java/Kotlin comments from code"""
    # Remove multi-line comments /* ... */
    content = re.sub(r'/\*[\s\S]*?\*/', '', content)
    # Remove single-line comments //
    content = re.sub(r'//.*?$', '', content, flags=re.MULTILINE)
    return content

def remove_html_comments(content):
    """Remove HTML comments from code"""
    content = re.sub(r'<!--[\s\S]*?-->', '', content)
    return content

def remove_c_comments(content):
    """Remove C/C++ comments from code"""
    # Remove multi-line comments /* ... */
    content = re.sub(r'/\*[\s\S]*?\*/', '', content)
    # Remove single-line comments //
    content = re.sub(r'//.*?$', '', content, flags=re.MULTILINE)
    return content

def remove_python_comments(content):
    """Remove Python comments from code"""
    # Remove single-line comments #
    content = re.sub(r'#.*?$', '', content, flags=re.MULTILINE)
    # Note: Triple quotes (docstrings) are more complex, leaving some in for context
    return content

def remove_javascript_comments(content):
    """Remove JavaScript comments from code"""
    # Remove multi-line comments /* ... */
    content = re.sub(r'/\*[\s\S]*?\*/', '', content)
    # Remove single-line comments //
    content = re.sub(r'//.*?$', '', content, flags=re.MULTILINE)
    return content

def clean_content(content, file_ext):
    """Clean file content by removing comments based on file type"""
    if file_ext in {'.java', '.kt'}:
        content = remove_java_comments(content)
    elif file_ext in {'.html', '.xml'}:
        content = remove_html_comments(content)
        content = remove_java_comments(content)
    elif file_ext in {'.c', '.h', '.cpp', '.hpp', '.cc'}:
        content = remove_c_comments(content)
    elif file_ext == '.py':
        content = remove_python_comments(content)
    elif file_ext in {'.js', '.json'}:
        content = remove_javascript_comments(content)
    elif file_ext in {'.gradle', '.properties', '.yaml', '.yml', '.sh', '.bat'}:
        content = remove_python_comments(content)
    
    # Remove excessive whitespace
    lines = content.split('\n')
    cleaned_lines = []
    for line in lines:
        # Strip trailing whitespace
        line = line.rstrip()
        # Skip empty lines (we'll add minimal spacing back)
        if line.strip():
            cleaned_lines.append(line)
    
    return '\n'.join(cleaned_lines)

def should_include_file(file_path, file_name):
    """Check if file should be included in the output"""
    if file_name in EXCLUDE_FILES:
        return False
    
    file_ext = Path(file_path).suffix.lower()
    return file_ext in INCLUDE_EXTENSIONS

def should_traverse_dir(dir_name):
    """Check if directory should be traversed"""
    return dir_name not in EXCLUDE_DIRS and not dir_name.startswith('.')

def scan_project():
    """Scan project and collect file information"""
    files_data = []
    
    for root, dirs, files in os.walk('.'):
        # Filter directories to traverse
        dirs[:] = [d for d in dirs if should_traverse_dir(d)]
        
        for file in files:
            file_path = os.path.join(root, file)
            
            if should_include_file(file_path, file):
                try:
                    with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                        content = f.read()
                    
                    files_data.append({
                        'path': file_path,
                        'content': content,
                        'extension': Path(file_path).suffix.lower()
                    })
                    print(f"✓ Processed: {file_path}")
                except Exception as e:
                    print(f"✗ Error reading {file_path}: {e}")
    
    return files_data

def generate_output(files_data):
    """Generate consolidated output with all project information"""
    output_lines = []
    
    # Header
    output_lines.append("=" * 80)
    output_lines.append("PROJECT CONTEXT FOR LLM - CI/CD SCRIPT GENERATION")
    output_lines.append("=" * 80)
    output_lines.append(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    output_lines.append(f"Total files processed: {len(files_data)}")
    output_lines.append("")
    
    # Project structure overview
    output_lines.append("=" * 80)
    output_lines.append("PROJECT STRUCTURE OVERVIEW")
    output_lines.append("=" * 80)
    
    # Count files by extension
    ext_count = {}
    for file_data in files_data:
        ext = file_data['extension']
        ext_count[ext] = ext_count.get(ext, 0) + 1
    
    output_lines.append("\nFiles by type:")
    for ext in sorted(ext_count.keys()):
        output_lines.append(f"  {ext}: {ext_count[ext]} files")
    
    output_lines.append("")
    output_lines.append("Files included in this context:")
    for file_data in sorted(files_data, key=lambda x: x['path']):
        output_lines.append(f"  - {file_data['path']}")
    
    output_lines.append("")
    output_lines.append("=" * 80)
    output_lines.append("PROJECT FILES CONTENT")
    output_lines.append("=" * 80)
    output_lines.append("")
    
    # Add file contents
    for file_data in sorted(files_data, key=lambda x: x['path']):
        file_path = file_data['path']
        content = file_data['content']
        ext = file_data['extension']
        
        # Clean content
        cleaned_content = clean_content(content, ext)
        
        # Add file section
        output_lines.append("-" * 80)
        output_lines.append(f"FILE: {file_path}")
        output_lines.append(f"TYPE: {ext}")
        output_lines.append("-" * 80)
        output_lines.append(cleaned_content)
        output_lines.append("")
        output_lines.append("")
    
    return "\n".join(output_lines)

def main():
    """Main execution function"""
    print("Starting project mapping...")
    print(f"Output will be saved to: {OUTPUT_FILE}")
    print("")
    
    # Scan project
    print("Scanning project files...")
    files_data = scan_project()
    
    if not files_data:
        print("No files found to process!")
        return
    
    print(f"\nFound {len(files_data)} files to process")
    
    # Generate output
    print("Generating consolidated output...")
    output_content = generate_output(files_data)
    
    # Write to file
    try:
        with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
            f.write(output_content)
        
        # Get file size
        file_size = os.path.getsize(OUTPUT_FILE)
        size_mb = file_size / (1024 * 1024)
        
        print(f"\n✓ Successfully created {OUTPUT_FILE}")
        print(f"  File size: {file_size:,} bytes ({size_mb:.2f} MB)")
        print(f"  Lines: {len(output_content.splitlines()):,}")
        
    except Exception as e:
        print(f"\n✗ Error writing output file: {e}")
        return
    
    print("\nProject mapping complete!")

if __name__ == "__main__":
    main()
