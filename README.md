# TaskPlannerPro

TaskPlannerPro is a modern Android task management application built with Jetpack Compose that helps users organize their tasks with advanced features like notifications, alarms, categories, and priorities.

## Features

### Task Management
- Create, edit, and delete tasks
- Set task title, description, start date, and end date
- Mark tasks as complete
- Bulk selection and deletion of tasks
- Task categories (Personal, Work, Shopping, etc.)
- Priority levels (High, Medium, Low)

### Notifications & Alarms
- Task start and end time notifications
- Custom alarm settings with sound selection
- Background alarm service
- Works even when device is locked
- Persistent notifications
- Custom notification sounds

### Organization & Filtering
- Filter tasks by category
- Sort by date or title
- Search functionality
- Show/hide completed tasks
- Priority-based filtering
- Category-based organization

### Security
- Encrypted data storage
- Secure preferences
- Protected task information
- Safe backup and restore

### UI/UX
- Material Design 3 implementation
- Dark/Light theme support
- Intuitive gesture controls
- Smooth animations
- Responsive layout

## Technical Details

### Architecture & Libraries
- MVVM Architecture
- Jetpack Compose UI
- Room Database
- Kotlin Coroutines & Flow
- Hilt Dependency Injection
- WorkManager for background tasks
- AndroidX Security

### Key Components
- Room Database for data persistence
- WorkManager for scheduling notifications
- Encrypted SharedPreferences for secure storage
- Background Service for reliable alarms
- BroadcastReceiver for system events

## Setup & Installation

1. Clone the repository: 
https://github.com/arvind-git-code/TaskPlannerPro.git


2. Open in Android Studio

3. Add required dependencies in app/build.gradle.kts

4. Sync project with Gradle files

5. Run on emulator or physical device

## Requirements
- Android Studio Arctic Fox or newer
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin version: 1.9.22

## Permissions
The app requires the following permissions:

xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />


## Screenshots
[Add your app screenshots here]

## Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Acknowledgments
- Material Design 3 Guidelines
- Android Jetpack libraries
- Open-source community

## Contact
Your Name - Arvind Maurya
Project Link: [https://github.com/arvind-git-code/TaskPlannerPro.git](https://github.com/arvind-git-code/TaskPlannerPro.git)

## Future Enhancements
- Task sharing functionality
- Cloud synchronization
- Task templates
- Statistics and analytics
- Collaboration features
- Task attachments
- Custom task categories
- Advanced recurring tasks