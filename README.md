# Ctrl+V+Win

FOP Hackathon 2026 / WIX1002 Group Project (Group 6, OCC 2): a Java console application with an AI feature.

> Items marked `TODO` are still to be filled in by the team.

## Project summary
- **Project:** Student E-Wallet (working title)
- **Track:** Campus Life & Student Services
- **UN SDGs:**
  - **SDG 8 (Decent Work and Economic Growth):** gives students and campus merchants a simple, low-cost cashless way to pay and get paid. Small campus businesses (cafes, food stalls) can accept payments without card terminals, and every account gets a clear transaction history and statement.
  - **SDG 4 (Quality Education):** the "Explain my spending" AI feature helps students understand where their money goes and build better budgeting habits, which supports financial literacy.
- **AI feature:** "Explain my spending". The AI reads a student's transaction history and gives a plain-language summary with saving tips. If the AI is unavailable, the app shows a fallback message and everything else keeps working.

## Team
| Name | GitHub | Role |
|---|---|---|
| Muhamad Aliff bin Mohd Sahari | aliffsahari | Data & Storage Developer |
| Nik Mohamad Azmizi bin Mohamad Mran | nikazmizi | Core Logic Developer |
| Noor Nabila binti Saniman | 26006219-bit | AI Integration Developer |
| Orin Nuraiya Nawar | n-orin | Interface & Quality Developer |

## Requirements
- Java JDK 17 or newer (check with `java -version`)
- An IDE such as NetBeans, or just a terminal
- Internet connection and an API key for the AI feature (optional: the program still works without them)

## Setup for the evaluator
1. Clone the repository:
   ```
   git clone https://github.com/aliffsahari/Ctrl-V-Win.git
   cd Ctrl-V-Win
   ```
2. Create your config file from the template:
   ```
   copy config.txt.example config.txt      (Windows)
   cp config.txt.example config.txt        (macOS / Linux)
   ```
3. Open `config.txt` and fill in `API_KEY`, `API_URL` and `MODEL`.
   If the key or internet is missing, the AI feature shows a fallback message and the rest of the program keeps working.

## How to compile and run
Run everything from the project root so the program finds `config.txt` and the `data/` folder.

```
javac -d out src/*.java
java -cp out Main
```

(TODO: update these commands if the team moves the classes into packages.)

Or open the project in NetBeans (or another IDE), set `Main` as the main class and press Run.

## Sample logins (fake data)
The `data/` folder comes with sample accounts so you can try the program straight away. All names, IDs and PINs are made up.

| ID | PIN | Role |
|---|---|---|
| 25000001 | 1234 | Student |
| 25000002 | 2345 | Student |
| 25000003 | 3456 | Student (frozen account) |
| M0001 | 4321 | Merchant |
| M0002 | 5432 | Merchant |

TODO: add the admin login once it is decided.

## Data files
All data is saved to `.txt` files and reloaded when the program restarts. Fields are separated by `|`, one record per line, with no header row. Lines that are empty or start with `#` are ignored.

| File | Purpose | Format |
|---|---|---|
| `data/accounts.txt` | All accounts | `STUDENT\|id\|name\|pin\|balance\|frozen\|dailyLimit` or `MERCHANT\|id\|name\|pin\|balance\|frozen\|businessName` |
| `data/transactions.txt` | Every top-up, payment, transfer and refund | `id\|type\|senderId\|receiverId\|amount\|fee\|dateTime\|status` |
| `reports/statement_<id>.txt` | Statement for one account (generated) | Formatted text report |
| `reports/admin_report.txt` | Overview for the admin (generated) | Formatted text report |
| `ai_log.txt` | Every AI reply shown to a user (generated, not in Git) | `[timestamp] PROMPT / REPLY` lines |
| `config.txt` | API settings (not in Git) | `KEY=value`, one per line |

Example account line: `STUDENT|25000001|Aisyah Rahman|1234|107.50|false|500.00`
Example transaction line: `T00001|TOPUP|25000001|25000001|100.00|0.00|2026-12-01 09:00|SUCCESS`

## Features
- **Student:** view balance, top up, pay a merchant, transfer to another student, view history, export a statement, explain my spending (AI)
- **Merchant:** view balance, view received payments, sales summary, refund
- **Admin:** list, create, freeze, unfreeze and delete accounts, generate the admin report

TODO: confirm the final feature list.

## Project structure
```
src/        Java source files
data/       Saved .txt data files (sample data included)
reports/    Generated statements and admin report
```
See `PROJECT_STRUCTURE.md` for what each part of the code does.

## Class overview
- **Model:** `Account` (abstract), `StudentAccount`, `MerchantAccount`, `Admin`, `Transaction`
- **Exceptions:** `InsufficientFundsException`, `DailyLimitExceededException`, `AccountFrozenException`, `AIServiceException`
- **Managers (plain arrays):** `AccountManager`, `TransactionManager`
- **Storage:** `FileManager`, `ReportGenerator`
- **AI:** `AIHelper`, `AILogger`, `SpendingAdvisor`
- **Interface:** `Main`, `StudentMenu`, `MerchantMenu`, `AdminMenu`, `InputValidator`

`Account` is the abstract parent class. `StudentAccount` and `MerchantAccount` override `pay()`, `calculateFee()` and `toFileString()` to behave differently.

## Known limitations
- TODO

## Notes
- Collections such as `ArrayList` and `HashMap` are not used for storing objects. Plain arrays with a separate counter are used instead.
- Do not commit `config.txt`. It contains the real API key.
- Passwords and PINs are stored as plain text because this is a class project. A real system would hash them.
