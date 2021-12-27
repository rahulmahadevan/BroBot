# CRBot

Project Overview:
A Maven Project developed using Java (Dependencies in pom.xml)
MongoDB local instance is used as database for the bot

The project is a telegram bot used to automate some of the tasks for online classwork by sending messages to telegram group. It has following functionality:
1. Send Notice announcements from NITW Website (Completed)
2. Send Daily time table with meeting links (Pending)
3. Reminders about class (Pending)
4. Announcements and reminders about Assignments/Exams (Completed)
5. Automated replies about FAQs from earlier discussions (Pending)

Data Flow:
A. Telegram Group ----(Message)-----> Java Telegram Bot API [Process message] ------(Response)-----> Telegram Group
B. Java [Periodically check for events 1,2,3,4] -----(Event occures)------> Scan DB, Prepare Message and Send Message ----------> Telegram Group
C. Learning: Admin (Provides data through Bot chat)----(Message)-----> Java Telegram Bot API stores data in MongoDB 
