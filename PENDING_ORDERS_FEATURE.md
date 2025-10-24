# Pending Orders Feature

## Overview
This feature adds a "Current Orders" section to the main page of the Foodivery app, similar to Swiggy's current order display. It shows pending/active orders with their status, progress indicators, and action buttons.

## Features

### 1. Current Orders Section
- Displays on the main page when user has pending orders
- Shows order number, status, restaurant name, items count, and total amount
- Includes a visual progress indicator showing order stages
- Provides "Track Order" and "View Details" buttons

### 2. Order Progress Tracking
- Visual progress bar showing 4 stages:
  - Ordered (green when completed)
  - Preparing (green when completed)
  - On the way (green when completed)
  - Delivered (green when completed)

### 3. Order Status Mapping
- Pending/PEN → "Processing"
- Preparing → "Preparing"
- Out for delivery/On the way → "On the way"
- Delivered → "Delivered"
- Cancelled/CNL → "Cancelled"

### 4. Auto-refresh
- Fetches pending orders when app starts
- Refreshes when user returns to main page
- Only shows orders that are not delivered or cancelled

## Implementation Details

### Files Added/Modified

#### New Files:
1. `app/src/main/res/layout/pending_order_card.xml` - Layout for individual pending order cards
2. `app/src/main/java/com/test/foodivery/Adapter/PendingOrderAdapter.java` - Adapter for pending orders RecyclerView

#### Modified Files:
1. `app/src/main/res/layout/activity_main.xml` - Added pending orders section
2. `app/src/main/java/com/test/foodivery/Activity/MainActivity.java` - Added pending orders functionality

### Key Components

#### MainActivity Changes:
- Added `fetchPendingOrders()` method to fetch current orders from API
- Added pending orders section initialization in `initializeViews()`
- Added refresh logic in `onResume()` method
- Uses existing `Attributes.Fetch_History` API endpoint

#### PendingOrderAdapter:
- Handles display of order information
- Manages progress indicator colors based on order status
- Provides click handlers for tracking and viewing order details
- Formats order time and status text

#### Layout Structure:
```xml
<!-- Pending Orders Section -->
<LinearLayout id="pending_orders_section">
    <TextView>Current Orders</TextView>
    <RecyclerView id="rv_pending_orders"/>
</LinearLayout>
```

### API Integration
- Uses existing `Attributes.Fetch_History` endpoint
- Filters orders to show only pending/active orders
- Parses order details including products and status information

### User Experience
- Section only appears when user has pending orders
- Seamlessly integrates with existing main page design
- Provides quick access to order tracking and details
- Maintains consistent UI with rest of the app

## Usage
1. User places an order
2. Returns to main page
3. "Current Orders" section appears with order details
4. User can track order progress or view full details
5. Section disappears when all orders are delivered/cancelled

## Future Enhancements
- Add real-time order status updates
- Include restaurant logos/names from order details
- Add estimated delivery time
- Implement push notifications for order status changes
- Add order cancellation functionality
