# My Arubaito  
**Track shifts, predict your income, and manage multiple jobs — all in one place.**

My Arubaito is a full-stack web app designed for part-time workers who deal with dynamic and inconsistent schedules. It helps you manage your shifts, organize multiple jobs, and instantly calculate your projected income.


## ⭐ About My Arubaito  
Keeping track of part-time work schedules can be messy — different jobs, changing hours, unpredictable pay periods. My Arubaito automates all of this by giving users a simple way to:

- Organize shifts across multiple jobs  
- Track hours and earnings  
- Import schedules using AI (from screenshots)  
- Predict future income  

Whether you're juggling jobs or planning your monthly budget, My Arubaito gives you a clear view of your work life.


## 🎯 Why I Built This  
I originally built this app because I was tired of how **slow and unreliable** my workplace’s scheduling app was. I wanted something faster, cleaner, and automatic.

My goals were:

- Zero-friction shift tracking  
- Accurate income prediction  
- Simple job management  
- Automated shift import using AI  
- A tool that actually helps part-time workers organize their lives

Over time, the project evolved into an iOS app + full-stack web app ecosystem with advanced features like auto-shift detection, widgets, and Apple Calendar sync.


## 🖥️ Screenshots  
![UI Screenshot](https://raw.githubusercontent.com/RochaLS/MyArubaitoDashboard/main/myarubaito-prev.png)
![UI Screenshot](https://raw.githubusercontent.com/RochaLS/MyArubaitoDashboard/main/myarubaito-prev2.png)
![UI Screenshot](https://raw.githubusercontent.com/RochaLS/MyArubaitoDashboard/main/myarubaito-prev3.png)
![UI Screenshot](https://raw.githubusercontent.com/RochaLS/MyArubaitoDashboard/main/myarubaito-prev4.png)
![UI Screenshot](https://raw.githubusercontent.com/RochaLS/MyArubaitoDashboard/main/myarubaito-prev5.png)

## 🛠️ Tech Stack  

### **Frontend**
- React  
- Next.js  
- TypeScript  
- ChakraUI

### **Backend**
- Java Spring Boot (REST API)  
- Spring Security (Session-based authentication) Currently updating to JWT Auth.
- MySQL (Railway hosted)  
- AWS KMS (Secure encryption for sensitive data)

### **AI Integration**
- Gemini API (Shift extraction from screenshots)

### **Infrastructure & Deployment**
- Backend: Railway  
- Frontend: Vercel  
- Environment encryption: AWS KMS

## 🧠 Key Features  
### ✔ Multi-job support  
Easily manage different workplaces, each with its own schedule and wage.

### ✔ Shift tracking  
Track all shifts by day, job, and shift type (open/mid/close).

### ✔ Income prediction  
Instantly calculate expected earnings for the current or upcoming pay period.

### ✔ AI-powered shift import  
Upload a screenshot of your workplace schedule — AI reads it and imports the shifts automatically.

### ✔ Clean schedule overview  
See upcoming shifts, pay projections, and job summaries at a glance.

## 🔐 Authentication & Security  
- JWT-based authentication  
- Encrypted user data using **AWS KMS**  
- HTTPS enforced end-to-end



## 🔧 Architecture Overview  

**Frontend**  
Communicates with the REST API to manage jobs, shifts, and user data.

**Backend**  
Handles business logic, authentication, shift processing, and data encryption.

**AI Layer**  
Gemini API parses schedule screenshots and transforms them into structured shift data.

**Database**  
MySQL stores all jobs, shifts, and user settings.

## 🚧 Upcoming Features  
- Automatic pay stub estimation  
- Calendar export & iCal sync   
- More detailed analytics and charts  
- Improved mobile experience  

