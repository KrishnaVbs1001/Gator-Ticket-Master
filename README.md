# Gator Ticket Master

Gator Ticket Master is a robust seat booking and reservation management system designed to manage seat allocation for Gator Events. This system implements efficient data structures such as Red-Black Trees and Min Heaps to handle reservations, waitlists, and available seats.

## Features
- **Red-Black Tree**: Efficient management of seat reservations with `O(log n)` complexity for insert, delete, and search operations.
- **Min Heap**: Priority-based waitlist management to ensure fair seat allocation.
- **Seat Management**: Dynamically allocates and reclaims seats.
- **Command-Driven Interface**: Supports commands for initialization, reservation, cancellation, and more.

## Key Operations
1. **Initialize**: Create the system with a specified number of seats.
2. **Available**: Display the count of available seats and the size of the waitlist.
3. **Reserve**: Book the lowest available seat or add users to the waitlist based on priority.
4. **Cancel**: Free up reserved seats and allocate them to the highest-priority waitlisted users.
5. **Add Seats**: Expand the system by adding new seats dynamically.
6. **Exit Waitlist**: Remove a user from the waitlist without assigning a seat.
7. **Update Priority**: Change the priority of a waitlisted user while maintaining order.
8. **Release Seats**: Release seats for a range of user IDs, reassigning them to waitlisted users.
9. **Print Reservations**: Display all current reservations sorted by seat number.
10. **Quit**: Gracefully terminate the program.

## Core Concepts
- **Data Structures**:
  - Red-Black Tree for managing reserved seats.
  - Min Heap for handling waitlists.
  - Seat Min Heap for managing available seats in ascending order.
- **Efficiency**: Most operations are optimized for `O(log n)` time complexity.
- **Command Processing**: Accepts commands via an input file for seamless integration.

## Usage
1. Compile the program:
   ```bash
   javac GatorTicketMaster.java
2. Run the program with an input file:<br>
   ```bash
   java GatorTicketMaster <input_file><br>
  Example: java GatorTicketMaster test1.txt
3. The system generates an output file named <input_file>_output_file.txt with results.

## Input File Format
Commands are read from the input file. Supported commands:

- **Initialize(n)** - Initialize the system with n seats.
- **Available()** - Display the count of available seats and waitlist length.
- **Reserve(userId, priority)** - Reserve a seat for userId or add to waitlist.
- **Cancel(seatId, userId)** - Cancel the reservation of a seat.
- **AddSeats(n)** - Add n new seats to the system.
- **ExitWaitlist(userId)** - Remove a user from the waitlist.
- **UpdatePriority(userId, priority)** - Update the priority of a waitlisted user.
- **ReleaseSeats(userID1, userID2)** - Release seats held by users in a range.
- **PrintReservations()** - Display all current reservations.
- **Quit()** - Terminate the program.

### Example
- Input file: commands.txt<br>
  Initialize(10)<br>
  Reserve(1, 5)<br>
  Reserve(2, 8)<br>
  Cancel(1, 1)<br>
  PrintReservations()<br>
  Quit()

- Output File: commands_output_file.txt<br>
  10 Seats are made available for reservation<br>
  User 1 reserved seat 1<br>
  User 2 reserved seat 2<br>
  User 1 canceled their reservation<br>
  Seat 2, User 2<br>
  Program Terminated!!<br>
  
# License
This project is licensed under the MIT License.
