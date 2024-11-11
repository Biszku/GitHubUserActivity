# GitHub User Activity Analysis

## Overview
Application that analyzes the activity of a GitHub user.
The application will display last 30 events of a user.

## Installation

1. Clone the repository
```bash
git clone https://github.com/Biszku/GitHubUserActivity.git
cd GitHubUserActivity
```
2. Compile the project
```bash
javac -d out -cp .\libs\gson-2.10.1.jar .\src\*.java
```
3. Run the project
```bash
java -cp "out;libs\gson-2.10.1.jar" GitHubActivity [github_username]
```
