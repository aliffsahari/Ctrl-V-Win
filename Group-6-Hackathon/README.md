# Group-6-Hackathon

FOP Hackathon 2026 / WIX1002 Group Project: a Java console application with an AI feature.

> Replace the placeholders below (marked with `TODO`) once your team has chosen a topic.

## Project summary
- **Project name:** TODO
- **Track:** TODO
- **UN SDG(s):** TODO (and a sentence on how the app addresses it)
- **AI feature:** TODO (what the AI does for the user)

## Team
| Name | Student ID | GitHub username | Main responsibility |
|---|---|---|---|
| TODO | TODO | TODO | AI integration (`AIHelper`, config, logging) |
| TODO | TODO | TODO | Backend: model classes and business rules |
| TODO | TODO | TODO | Backend: arrays, managers, file I/O, reports |
| TODO | TODO | TODO | Menus, input validation, testing |

## Requirements
- Java JDK 17 or newer (check with `java -version`)
- An IDE such as NetBeans, or just a terminal
- Internet connection and an API key for the AI feature (optional: the program still works without them)

## Setup for the evaluator
1. Clone the repository:
   ```
   git clone https://github.com/aliffsahari/Group-6-Hackathon.git
   cd Group-6-Hackathon
   ```
2. Create your config file from the template:
   ```
   copy config.txt.example config.txt      (Windows)
   cp config.txt.example config.txt        (macOS / Linux)
   ```
3. Open `config.txt` and fill in `API_KEY`, `API_URL` and `MODEL`.
   If the key or internet is missing, the AI feature shows a fallback message and the rest of the program keeps working.

## How to compile and run
From the project root:

```
javac -d out src/*.java
java -cp out Main
```

Or open the project in NetBeans (or another IDE), set `Main` as the main class and press Run.
Run the program from the project root so it finds `config.txt` and the data files.

## Data files
All data is saved to `.txt` files in the working directory and reloaded on startup.

| File | Purpose | Format |
|---|---|---|
| `config.txt` | API settings (not in Git) | `KEY=value`, one per line |
| `ai_log.txt` | Every AI reply shown to a user (generated) | `[timestamp] reply text` |
| TODO | TODO | TODO (document each field and the delimiter) |

## Features
- TODO: list the main features per user role (e.g. Student, Merchant, Admin)

## Class overview
TODO: list the classes and one line on each. Mention the abstract class and its subclasses.

## Known limitations
- TODO

## Notes
- Collections such as `ArrayList` and `HashMap` are not used for storing objects. Plain arrays with a separate counter are used instead.
- Do not commit `config.txt`. It contains the real API key.
