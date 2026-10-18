package com.example.data.model

enum class UserRole(val displayName: String, val description: String) {
    ADMIN("Parent / Admin", "Full control over budget, chores, devices, and family settings"),
    TEEN("Teen", "Can complete tasks, add shopping items, and view calendar"),
    CHILD("Child", "Gamified chore checklist, earn stars and reward points"),
    SENIOR("Senior / Elder", "Simplified high-contrast interface, medication alerts, vital signs")
}

enum class TaskPriority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent Repair", 4)
}

enum class TaskCategory(val label: String, val iconName: String) {
    CLEANING("Cleaning", "ic_cleaning"),
    KITCHEN("Kitchen", "ic_kitchen"),
    MAINTENANCE("Maintenance", "ic_maintenance"),
    YARD("Yard & Garden", "ic_yard"),
    LAUNDRY("Laundry", "ic_laundry"),
    PETS("Pets", "ic_pets"),
    SAFETY("Safety & Inspect", "ic_safety")
}

enum class DeviceType(val label: String) {
    ROBOT_VACUUM("Robot Vacuum"),
    DISHWASHER("Smart Dishwasher"),
    HVAC_FILTER("HVAC Air Filter"),
    SMOKE_DETECTOR("Smoke & CO Detector"),
    SMART_FRIDGE("Smart Refrigerator"),
    WASHING_MACHINE("Smart Washer")
}

enum class DeviceStatus(val label: String) {
    NORMAL("Ready"),
    RUNNING("Running"),
    NEEDS_ATTENTION("Needs Attention"),
    WARNING("Urgent Alert")
}

enum class ExpenseCategory(val label: String) {
    MAINTENANCE("Repairs & Maintenance"),
    GROCERIES("Groceries"),
    UTILITIES("Utilities & Bills"),
    SUPPLIES("Household Supplies"),
    HEALTH("Health & Pharmacy"),
    OTHER("Other")
}

enum class AppLanguage(val code: String, val label: String, val flag: String) {
    EN("en", "English", "🇺🇸"),
    ES("es", "Español", "🇪🇸"),
    DE("de", "Deutsch", "🇩🇪"),
    FR("fr", "Français", "🇫🇷"),
    IT("it", "Italiano", "🇮🇹"),
    ZH("zh", "中文", "🇨🇳")
}
