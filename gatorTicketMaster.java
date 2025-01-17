
/**
 * GatorTicketMaster - A seat booking and reservation management system
 * This system implements a seat booking service with the following features:
 * - Red-Black Tree for managing seat reservations
 * - Min Heap for managing waitlist based on user priorities
 * - Min Heap for managing available seats in ascending order
 * Time Complexity: Most operations run in O(log n) time
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class gatorTicketMaster {
    // Core data structures for the reservation system
    private RedBlackTree reservations; // Stores reserved seat information
    private MinHeap waitlist; // Manages waitlisted users with priorities
    private SeatMinHeap availableSeats; // Manages available seats in ascending order
    private int totalSeats; // Total number of seats in the system
    private BufferedWriter output; // Handles output file writing

    /**
     * Constructor initializes all data structures for the booking system
     * @param outputFile Path to the output file
     */

    public gatorTicketMaster(String outputFile) throws IOException {
        reservations = new RedBlackTree();
        waitlist = new MinHeap();
        availableSeats = new SeatMinHeap();
        totalSeats = 0;
        output = new BufferedWriter(new FileWriter(outputFile));
    }

    /**
     * Node class for Red-Black Tree implementation
     * Each node represents a seat reservation with user and seat information
     */
    private static class RBNode {
        int userId; // Unique identifier for user
        int seatId; // Seat number assigned to user
        boolean isRed; // Color of node in Red-Black Tree
        RBNode left, right, parent; // Tree pointers

        RBNode(int userId, int seatId) {
            this.userId = userId;
            this.seatId = seatId;
            this.isRed = true;
            this.left = this.right = this.parent = null;
        }
    }

    /**
     * Node class for MinHeap implementations
     * Used in both waitlist (priority-based) and available seats (seat number-based)
     */
    private static class HeapNode {
        int priority; // User's priority in waitlist
        int userId; // User identifier
        int seatId; // Seat number (used in available seats)
        long timestamp; // Time of addition for tie-breaking

        HeapNode(int userId, int priority) {
            this.userId = userId;
            this.priority = priority;
            this.timestamp = System.nanoTime();
        }
    }

    /**
     * Red-Black Tree implementation for managing seat reservations
     * Provides O(log n) time complexity for insertions, deletions, and searches
     */
    private static class RedBlackTree {
        private RBNode root;

        /**
         * Performs left rotation around node x
         * @param x Node around which rotation is performed
         * Time Complexity: O(1)
         */
        private void leftRotate(RBNode x) {
            RBNode y = x.right;
            x.right = y.left;
            if (y.left != null)
                y.left.parent = x;
            y.parent = x.parent;
            if (x.parent == null)
                root = y;
            else if (x == x.parent.left)
                x.parent.left = y;
            else
                x.parent.right = y;
            y.left = x;
            x.parent = y;
        }

        /**
         * Performs right rotation around node y
         * @param y Node around which rotation is performed
         * Time Complexity: O(1)
         */
        private void rightRotate(RBNode y) {
            RBNode x = y.left;
            y.left = x.right;
            if (x.right != null)
                x.right.parent = y;
            x.parent = y.parent;
            if (y.parent == null)
                root = x;
            else if (y == y.parent.right)
                y.parent.right = x;
            else
                y.parent.left = x;
            x.right = y;
            y.parent = x;
        }

        /**
         * Fixes Red-Black Tree violations after insertion through color changes and rotations
         * @param node Newly inserted node
         * Time Complexity: O(log n)
         */
        private void fixViolation(RBNode node) {
            RBNode parent = null;
            RBNode grandParent = null;

            while (node != root && node.isRed && node.parent.isRed) {
                parent = node.parent;
                grandParent = parent.parent;

                if (parent == grandParent.left) {
                    RBNode uncle = grandParent.right;
                    // Case 1: Uncle is red - Only recoloring needed
                    if (uncle != null && uncle.isRed) {
                        grandParent.isRed = true;
                        parent.isRed = false;
                        uncle.isRed = false;
                        node = grandParent;
                    } else {
                        // Case 2: Node is right child - Left rotation needed
                        if (node == parent.right) {
                            leftRotate(parent);
                            node = parent;
                            parent = node.parent;
                        }
                        // Case 3: Node is left child - Right rotation needed
                        rightRotate(grandParent);
                        boolean tempColor = parent.isRed;
                        parent.isRed = grandParent.isRed;
                        grandParent.isRed = tempColor;
                        node = parent;
                    }
                } else {
                    // Mirror cases when parent is right child
                    RBNode uncle = grandParent.left;
                    // Similar cases with left/right exchanged
                    if (uncle != null && uncle.isRed) {
                        grandParent.isRed = true;
                        parent.isRed = false;
                        uncle.isRed = false;
                        node = grandParent;
                    } else {
                        if (node == parent.left) {
                            rightRotate(parent);
                            node = parent;
                            parent = node.parent;
                        }
                        leftRotate(grandParent);
                        boolean tempColor = parent.isRed;
                        parent.isRed = grandParent.isRed;
                        grandParent.isRed = tempColor;
                        node = parent;
                    }
                }
            }
            root.isRed = false; // Ensure root remains black
        }

        /**
         * Inserts a new reservation into the Red-Black Tree
         * @param userId User ID for the reservation
         * @param seatId Seat ID assigned to the user
         * Time Complexity: O(log n)
         */
        public void insert(int userId, int seatId) {
            RBNode newNode = new RBNode(userId, seatId);
            // Special case: empty tree
            if (root == null) {
                root = newNode;
                root.isRed = false;
                return;
            }

            RBNode current = root;
            RBNode parent = null;

            while (current != null) {
                parent = current;
                if (userId < current.userId)
                    current = current.left;
                else
                    current = current.right;
            }

            newNode.parent = parent;
            if (userId < parent.userId)
                parent.left = newNode;
            else
                parent.right = newNode;

            fixViolation(newNode);
        }

        /**
         * Searches for a reservation by user ID
         * @param userId User ID to search for
         * @return Node containing the reservation or null if not found
         * Time Complexity: O(log n)
         */
        public RBNode search(int userId) {
            RBNode current = root;
            while (current != null) {
                if (userId == current.userId)
                    return current;
                if (userId < current.userId)
                    current = current.left;
                else
                    current = current.right;
            }
            return null;
        }

        /**
         * Finds the successor node (smallest key larger than given node)
         * Used in deletion operation
         * @param node Node whose successor is needed
         * @return Successor node
         * Time Complexity: O(log n)
         */
        private RBNode findSuccessor(RBNode node) {
            RBNode successor = node.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            return successor;
        }

        /**
         * Replaces one subtree with another
         * Used as a helper method in deletion
         * @param u Subtree to be replaced
         * @param v Replacement subtree
         * Time Complexity: O(1)
         */
        private void transplant(RBNode u, RBNode v) {
            if (u.parent == null) {
                root = v;
            } else if (u == u.parent.left) {
                u.parent.left = v;
            } else {
                u.parent.right = v;
            }
            if (v != null) {
                v.parent = u.parent;
            }
        }

        /**
         * Fixes Red-Black Tree violations after deletion
         * @param x Node at which violation might occur
         * @param parent Parent of node x
         * Time Complexity: O(log n)
         */
        private void fixDelete(RBNode x, RBNode parent) {
            // Handles four cases:
            // 1. Sibling is red
            // 2. Sibling is black with black children
            // 3. Sibling is black with red left child, black right child
            // 4. Sibling is black with red right child
            while (x != root && (x == null || !x.isRed)) {
                if (x == parent.left) {
                    RBNode w = parent.right;
                    if (w.isRed) {
                        // Case 1: x's sibling w is red
                        w.isRed = false;
                        parent.isRed = true;
                        leftRotate(parent);
                        w = parent.right;
                    }

                    if ((w.left == null || !w.left.isRed) &&
                            (w.right == null || !w.right.isRed)) {
                        // Case 2: x's sibling w is black, and both of w's children are black
                        w.isRed = true;
                        x = parent;
                        parent = x.parent;
                    } else {
                        if (w.right == null || !w.right.isRed) {
                            // Case 3: x's sibling w is black, w's left child is red,
                            // and w's right child is black
                            if (w.left != null) {
                                w.left.isRed = false;
                            }
                            w.isRed = true;
                            rightRotate(w);
                            w = parent.right;
                        }
                        // Case 4: x's sibling w is black, and w's right child is red
                        w.isRed = parent.isRed;
                        parent.isRed = false;
                        if (w.right != null) {
                            w.right.isRed = false;
                        }
                        leftRotate(parent);
                        x = root;
                        parent = null;
                    }
                } else {
                    // Same as above with "right" and "left" exchanged
                    RBNode w = parent.left;
                    if (w.isRed) {
                        w.isRed = false;
                        parent.isRed = true;
                        rightRotate(parent);
                        w = parent.left;
                    }

                    if ((w.right == null || !w.right.isRed) &&
                            (w.left == null || !w.left.isRed)) {
                        w.isRed = true;
                        x = parent;
                        parent = x.parent;
                    } else {
                        if (w.left == null || !w.left.isRed) {
                            if (w.right != null) {
                                w.right.isRed = false;
                            }
                            w.isRed = true;
                            leftRotate(w);
                            w = parent.left;
                        }
                        w.isRed = parent.isRed;
                        parent.isRed = false;
                        if (w.left != null) {
                            w.left.isRed = false;
                        }
                        rightRotate(parent);
                        x = root;
                        parent = null;
                    }
                }
            }
            if (x != null) {
                x.isRed = false;
            }
        }

        /**
         * Deletes a node from the Red-Black Tree
         * Maintains Red-Black properties after deletion
         * @param z Node to be deleted
         * Time Complexity: O(log n)
         */
        public void delete(RBNode z) {
            if (z == null)
                return;

            RBNode x;
            RBNode parent;
            boolean originalColor = z.isRed;

            if (z.left == null) {
                // Case 1: No left child
                x = z.right;
                parent = z.parent;
                transplant(z, z.right);
            } else if (z.right == null) {
                // Case 2: No right child
                x = z.left;
                parent = z.parent;
                transplant(z, z.left);
            } else {
                // Case 3: Both children exist
                RBNode y = findSuccessor(z);
                originalColor = y.isRed;
                x = y.right;

                if (y.parent == z) {
                    parent = y;
                } else {
                    parent = y.parent;
                    transplant(y, y.right);
                    y.right = z.right;
                    if (y.right != null) {
                        y.right.parent = y;
                    }
                }

                transplant(z, y);
                y.left = z.left;
                y.left.parent = y;
                y.isRed = z.isRed;
            }

            // If we deleted a black node, we need to fix the tree
            if (!originalColor) {
                fixDelete(x, parent);
            }
        }
    }

    /**
     * MinHeap implementation for waitlist management
     * Maintains users in order of priority (higher number = higher priority)
     * Uses timestamp for tie-breaking in equal priorities
     */
    private static class MinHeap {
        private ArrayList<HeapNode> heap;

        public MinHeap() {
            heap = new ArrayList<>();
        }

        // Swaps two elements in the Heap
        private void swap(int i, int j) {
            HeapNode temp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, temp);
        }

        /**
         * Compares priorities of two nodes
         * Higher priority numbers take precedence
         * For equal priorities, earlier timestamp wins
         * @return true if a has higher priority than b
         * Time Complexity: O(1)
         */
        private boolean hasHigherPriority(HeapNode a, HeapNode b) {
            // Higher priority number should come first
            if (a.priority != b.priority) {
                return a.priority > b.priority;
            }
            // For equal priorities, earlier timestamp should come first
            return a.timestamp < b.timestamp;
        }

        /**
         * Maintains heap property by moving node up
         * Used after insertion or priority update
         * @param index Index of node to move up
         * Time Complexity: O(log n)
         */
        private void heapifyUp(int index) {
            while (index > 0) {
                int parent = (index - 1) / 2;
                // If current node has higher priority than parent, swap
                if (hasHigherPriority(heap.get(index), heap.get(parent))) {
                    swap(index, parent);
                    index = parent;
                } else {
                    break;
                }
            }
        }

        /**
         * Maintains heap property by moving node down
         * Used after extraction or priority update
         * @param index Index of node to move down
         * Time Complexity: O(log n)
         */
        private void heapifyDown(int index) {
            int size = heap.size();
            while (true) {
                int largest = index;
                int left = 2 * index + 1;
                int right = 2 * index + 2;

                if (left < size && hasHigherPriority(heap.get(left), heap.get(largest))) {
                    largest = left;
                }
                if (right < size && hasHigherPriority(heap.get(right), heap.get(largest))) {
                    largest = right;
                }

                if (largest == index) {
                    break;
                }

                swap(index, largest);
                index = largest;
            }
        }

        /**
         * Inserts a new user into the waitlist
         * @param userId   ID of user to insert
         * @param priority Priority value for the user
         * Time Complexity: O(log n)
         */
        public void insert(int userId, int priority) {
            HeapNode node = new HeapNode(userId, priority);
            heap.add(node);
            heapifyUp(heap.size() - 1);
        }

        /**
         * Extracts and returns highest priority user
         * @return Highest priority node or null if heap is empty
         * Time Complexity: O(log n)
         */
        public HeapNode extractMin() {
            if (heap.isEmpty()) {
                return null;
            }

            HeapNode result = heap.get(0);
            HeapNode last = heap.remove(heap.size() - 1);

            if (!heap.isEmpty()) {
                heap.set(0, last);
                heapifyDown(0);
            }

            return result;
        }

        // Returns current size of the heap
        // Time Complexity: O(1)
        public int size() {
            return heap.size();
        }

        // Checks if Heap is empty
        // Time Complexity: O(1)
        public boolean isEmpty() {
            return heap.isEmpty();
        }

        /**
         * Updates priority of a user while maintaining original timestamp
         * @param userId ID of user whose priority needs updating
         * @param newPriority New priority value
         * Time Complexity: O(n) for search + O(log n) for heap maintenance
         */
        public void updatePriority(int userId, int newPriority) {
            int index = -1;
            // Find user in heap
            for (int i = 0; i < heap.size(); i++) {
                if (heap.get(i).userId == userId) {
                    index = i;
                    break;
                }
            }

            // Only update if user is found
            if (index != -1) {
                long oldTimestamp = heap.get(index).timestamp; // Preserve timestamp
                heap.get(index).priority = newPriority;
                heap.get(index).timestamp = oldTimestamp;
                heapifyUp(index);
                heapifyDown(index);
            }
        }

        /**
         * Removes a user from the waitlist
         * @param userId ID of user to remove
         * Time Complexity: O(n) for search + O(log n) for heap maintenance
         */
        public void remove(int userId) {
            int index = -1;
            for (int i = 0; i < heap.size(); i++) {
                if (heap.get(i).userId == userId) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                swap(index, heap.size() - 1);
                heap.remove(heap.size() - 1);
                if (index < heap.size()) {
                    heapifyUp(index);
                    heapifyDown(index);
                }
            }
        }
    }

    /**
     * SeatMinHeap implementation for managing available seats
     * Maintains seats in ascending order for assignment
     * Ensures lowest numbered seat is assigned first
     * Provides O(log n) operations for seat allocation and release
     */
    private static class SeatMinHeap {
        private ArrayList<HeapNode> heap;

        public SeatMinHeap() {
            heap = new ArrayList<>();
        }

        // Swaps two seats in the Heap
        // Time Complexity: O(1)
        private void swap(int i, int j) {
            HeapNode temp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, temp);
        }

        /**
         * Maintains min-heap property by moving seat up
         * Ensures lower seat numbers are closer to root
         * @param index Index of seat to move up
         * Time Complexity: O(log n)
         */
        private void heapifyUp(int index) {
            while (index > 0) {
                int parent = (index - 1) / 2;
                // For seats, lower number should come first
                if (heap.get(index).seatId < heap.get(parent).seatId) {
                    swap(index, parent);
                    index = parent;
                } else {
                    break;
                }
            }
        }

        /**
         * Maintains min-heap property by moving seat down
         * Used after extraction to restore heap property
         * @param index Index of seat to move down
         * Time Complexity: O(log n)
         */
        private void heapifyDown(int index) {
            int size = heap.size();
            while (true) {
                int smallest = index;
                int left = 2 * index + 1;
                int right = 2 * index + 2;

                if (left < size && heap.get(left).seatId < heap.get(smallest).seatId) {
                    smallest = left;
                }
                if (right < size && heap.get(right).seatId < heap.get(smallest).seatId) {
                    smallest = right;
                }

                if (smallest == index) {
                    break;
                }

                swap(index, smallest);
                index = smallest;
            }
        }

        /**
         * Adds a new available seat to the heap
         * Time Complexity: O(log n)
         */
        public void insert(int seatId, int priority) { // priority is unused for seats
            HeapNode node = new HeapNode(seatId, priority);
            node.seatId = seatId; // Ensure seatId is set
            heap.add(node);
            heapifyUp(heap.size() - 1);
        }

        /**
         * Removes and returns the lowest numbered available seat
         * @return Node containing the lowest available seat number, or null if no seats available
         * Time Complexity: O(log n)
         */
        public HeapNode extractMin() {
            if (heap.isEmpty()) {
                return null;
            }

            HeapNode min = heap.get(0);
            HeapNode last = heap.remove(heap.size() - 1);

            if (!heap.isEmpty()) {
                heap.set(0, last);
                heapifyDown(0);
            }

            return min;
        }

        // Checks if there are any available seats
        // Time Complexity: O(1)
        public boolean isEmpty() {
            return heap.isEmpty();
        }

        // Returns the number of available seats
        // Time Complexity: O(1)
        public int size() {
            return heap.size();
        }
    }

    // Core operation methods

    /**
     * Initializes the booking system with specified number of seats
     * @param seatCount Number of seats to initialize
     * Time Complexity: O(n) where n is seatCount
     */
    public void initialize(int seatCount) throws IOException {
        if (seatCount <= 0) {
            output.write("Invalid input. Please provide a valid number of seats.\n");
            return;
        }

        totalSeats = seatCount;
        // Clear existing data structures
        waitlist = new MinHeap();
        availableSeats = new SeatMinHeap();
        reservations = new RedBlackTree();

        // Add seats in ascending order
        for (int i = 1; i <= seatCount; i++) {
            availableSeats.insert(i, i); // Using consistent method signature
        }

        output.write(seatCount + " Seats are made available for reservation\n");
        output.flush();
    }

    /**
     * Displays current available seats and waitlist length
     * Time Complexity: O(1)
     */
    public void available() throws IOException {
        int availableCount = availableSeats.size();
        int waitlistCount = waitlist.size();
        output.write(String.format("Total Seats Available : %d, Waitlist : %d\n",
                availableCount, waitlistCount));
        output.flush(); // Ensure the output is written immediately
    }

    /**
     * Handles seat reservation requests
     * - Assigns lowest available seat if available
     * - Adds user to waitlist if no seats available
     * @param userId User requesting reservation
     * @param userPriority User's priority for waitlist
     * Time Complexity: O(log n)
     */
    public void reserve(int userId, int userPriority) throws IOException {
        if (!availableSeats.isEmpty()) {
            HeapNode seat = availableSeats.extractMin();
            reservations.insert(userId, seat.seatId);
            output.write(String.format("User %d reserved seat %d\n", userId, seat.seatId));
        } else {
            waitlist.insert(userId, userPriority);
            output.write(String.format("User %d is added to the waiting list\n", userId));
        }
        output.flush();
    }

    /**
     * Processes cancellation of a seat reservation
     * - Validates if user has the specified seat reservation
     * - Reassigns seat to highest priority waitlisted user if waitlist exists
     * - Adds seat back to available seats if no waitlist
     * @param seatId The seat number to be cancelled
     * @param userId The user cancelling their reservation
     * Time Complexity: O(log n) for tree operations
     */
    public void cancel(int seatId, int userId) throws IOException {
        RBNode node = reservations.search(userId);
        if (node == null) {
            output.write(String.format("User %d has no reservation to cancel\n", userId));
            return;
        }

        if (node.seatId != seatId) {
            output.write(String.format("User %d has no reservation for seat %d to cancel\n",
                    userId, seatId));
            return;
        }

        reservations.delete(node);
        if (waitlist.isEmpty()) {
            availableSeats.insert(seatId, seatId); // Using consistent method signature
            output.write(String.format("User %d canceled their reservation\n", userId));
        } else {
            HeapNode nextUser = waitlist.extractMin();
            reservations.insert(nextUser.userId, seatId);
            output.write(String.format("User %d canceled their reservation\n", userId));
            output.write(String.format("User %d reserved seat %d\n", nextUser.userId, seatId));
        }
        output.flush();
    }

    /**
     * Removes a user from the waitlist
     * - Checks if user is in waitlist
     * - Removes user if found
     * - Maintains heap properties after removal
     * @param userId The user to be removed from waitlist
     * Time Complexity: O(n) for searching + O(log n) for heap maintenance
     */
    public void exitWaitlist(int userId) throws IOException {
        int initialSize = waitlist.size();
        waitlist.remove(userId);
        if (waitlist.size() < initialSize) {
            output.write(String.format("User %d is removed from the waiting list\n", userId));
        } else {
            output.write(String.format("User %d is not in waitlist\n", userId));
        }
    }

    /**
     * Updates priority of a user in the waitlist
     * - Verifies user is in waitlist
     * - Updates priority while maintaining timestamp
     * - Rebalances heap to maintain priority order
     * @param userId User whose priority needs to be updated
     * @param userPriority New priority value
     * Time Complexity: O(n) for searching + O(log n) for heap maintenance
     */
    public void updatePriority(int userId, int userPriority) throws IOException {
        // Check if user is in waitlist first
        boolean userInWaitlist = false;
        for (HeapNode node : waitlist.heap) {
            if (node.userId == userId) {
                userInWaitlist = true;
                break;
            }
        }

        if (!userInWaitlist) {
            output.write(String.format("User %d priority is not updated\n", userId));
        } else {
            waitlist.updatePriority(userId, userPriority);
            output.write(String.format("User %d priority has been updated to %d\n",
                    userId, userPriority));
        }
        output.flush();
    }

    /**
     * Adds new seats to the system
     * - Validates seat count
     * - Assigns new seats to waitlisted users based on priority
     * - Adds remaining seats to available seats pool
     * Uses temporary PriorityQueue for proper priority-based assignment
     * @param count Number of new seats to add
     * Time Complexity: O(k log k) where k is number of new seats
     */
    public void addSeats(int count) throws IOException {
        if (count <= 0) {
            output.write("Invalid input. Please provide a valid number of seats.\n");
            return;
        }

        int newSeatStart = totalSeats + 1;
        totalSeats += count;

        output.write(String.format("Additional %d Seats are made available for reservation\n", count));

        // Get highest priority users first
        PriorityQueue<HeapNode> tempQueue = new PriorityQueue<>((a, b) -> {
            if (a.priority != b.priority) {
                return b.priority - a.priority; // Higher priority first
            }
            return Long.compare(a.timestamp, b.timestamp); // Earlier timestamp wins ties
        });

        while (!waitlist.isEmpty() && tempQueue.size() < count) {
            HeapNode node = waitlist.extractMin();
            tempQueue.offer(node);
        }

        // Assign seats in ascending order to highest priority users
        int currentSeat = newSeatStart;
        while (!tempQueue.isEmpty()) {
            HeapNode nextUser = tempQueue.poll();
            reservations.insert(nextUser.userId, currentSeat);
            output.write(String.format("User %d reserved seat %d\n",
                    nextUser.userId, currentSeat));
            currentSeat++;
        }

        // Add remaining seats to available seats
        while (currentSeat <= totalSeats) {
            availableSeats.insert(currentSeat, currentSeat);
            currentSeat++;
        }
        output.flush();
    }

    /**
     * Prints all current seat reservations
     * - Performs inorder traversal of reservation tree
     * - Sorts results by seat number
     * - Outputs in specified format
     * Time Complexity: O(n log n) due to sorting
     */
    public void printReservations() throws IOException {
        List<RBNode> allReservations = new ArrayList<>();
        inorderTraversal(reservations.root, allReservations);

        // Sort by seat ID
        Collections.sort(allReservations, (a, b) -> Integer.compare(a.seatId, b.seatId));

        // Print reservations
        for (RBNode node : allReservations) {
            output.write(String.format("Seat %d, User %d\n", node.seatId, node.userId));
        }
        output.flush();
    }

    /**
     * Helper method for printReservations
     * Performs inorder traversal of Red-Black Tree
     * @param node   Current node in traversal
     * @param result List to store traversal results
     * Time Complexity: O(n) where n is number of nodes
     */
    private void inorderTraversal(RBNode node, List<RBNode> result) {
        if (node != null) {
            inorderTraversal(node.left, result);
            result.add(node);
            inorderTraversal(node.right, result);
        }
    }

    /**
     * Releases seats for a range of user IDs
     * - Collects all seats held by users in the range
     * - Removes users from both reservations and waitlist
     * - Reassigns released seats to waiting users by priority
     * - Adds remaining seats to available seats
     * @param userID1 Start of user ID range
     * @param userID2 End of user ID range (inclusive)
     * Time Complexity: O(m log n) where m is range size, n is totalusers
     */
    public void releaseSeats(int userID1, int userID2) throws IOException {
        if (userID1 > userID2) {
            output.write("Invalid input. Please provide a valid range of users.\n");
            return;
        }

        List<Integer> releasedSeats = new ArrayList<>();
        List<Integer> releasedSeatsSorted = new ArrayList<>();
        Map<Integer, Integer> seatUserMap = new HashMap<>();

        // First pass: collect all seats to be released
        for (int userId = userID1; userId <= userID2; userId++) {
            RBNode node = reservations.search(userId);
            if (node != null) {
                releasedSeats.add(node.seatId);
                releasedSeatsSorted.add(node.seatId);
                seatUserMap.put(node.seatId, userId);
                reservations.delete(node);
            }
            // Remove from waitlist if present
            waitlist.remove(userId);
        }
        // Handle empty case
        if (releasedSeats.isEmpty() && waitlist.isEmpty()) {
            output.write(String.format(
                    "Reservations/waitlist of the users in the range [%d, %d] have been released\n",
                    userID1, userID2));
            return;
        }

        // Reassignment Phase
        output.write(String.format(
                "Reservations of the Users in the range [%d, %d] are released\n",
                userID1, userID2));

        Collections.sort(releasedSeatsSorted); // Sort seats in ascending order

        // Assign seats to waiting users
        while (!releasedSeatsSorted.isEmpty() && !waitlist.isEmpty()) {
            HeapNode nextUser = waitlist.extractMin();
            int nextSeat = releasedSeatsSorted.remove(0);
            reservations.insert(nextUser.userId, nextSeat);
            output.write(String.format("User %d reserved seat %d\n",
                    nextUser.userId, nextSeat));
        }

        // Add remaining seats to available pool
        for (int seatId : releasedSeatsSorted) {
            availableSeats.insert(seatId, seatId);
        }
        output.flush();
    }

    /**
     * Main method - Entry point of the program
     * Handles:
     * - Command line argument validation
     * - Input file reading
     * - Command parsing and execution
     * - Output file management
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java GatorTicketMaster <input_file>");
            return;
        }

        try {
            String inputFile = args[0];
            String outputFile = inputFile.substring(0, inputFile.lastIndexOf('.')) + "_output_file.txt";

            gatorTicketMaster system = new gatorTicketMaster(outputFile);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Remove any leading/trailing whitespace

                try {
                    if (line.startsWith("Initialize")) {
                        // Extract number between parentheses
                        String countStr = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                        int count = Integer.parseInt(countStr.trim());
                        system.initialize(count);
                    } else if (line.equals("Available()")) {
                        system.available();
                    } else if (line.startsWith("Reserve")) {
                        // Extract parameters between parentheses
                        String paramsStr = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                        String[] params = paramsStr.split(",");
                        int userId = Integer.parseInt(params[0].trim());
                        int priority = Integer.parseInt(params[1].trim());
                        system.reserve(userId, priority);
                    } else if (line.startsWith("Cancel")) {
                        String paramsStr = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                        String[] params = paramsStr.split(",");
                        int seatId = Integer.parseInt(params[0].trim());
                        int userId = Integer.parseInt(params[1].trim());
                        system.cancel(seatId, userId);
                    } else if (line.startsWith("ExitWaitlist")) {
                        String userIdStr = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                        int userId = Integer.parseInt(userIdStr.trim());
                        system.exitWaitlist(userId);
                    } else if (line.startsWith("UpdatePriority")) {
                        String paramsStr = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                        String[] params = paramsStr.split(",");
                        int userId = Integer.parseInt(params[0].trim());
                        int priority = Integer.parseInt(params[1].trim());
                        system.updatePriority(userId, priority);
                    } else if (line.startsWith("AddSeats")) {
                        String countStr = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                        int count = Integer.parseInt(countStr.trim());
                        system.addSeats(count);
                    } else if (line.equals("PrintReservations()")) {
                        system.printReservations();
                    } else if (line.startsWith("ReleaseSeats")) {
                        String paramsStr = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                        String[] params = paramsStr.split(",");
                        int userId1 = Integer.parseInt(params[0].trim());
                        int userId2 = Integer.parseInt(params[1].trim());
                        system.releaseSeats(userId1, userId2);
                    } else if (line.equals("Quit()")) {
                        system.output.write("Program Terminated!!\n");
                        break;
                    }

                    // Flush the output after each command
                    system.output.flush();

                } catch (Exception e) {
                    System.err.println("Error processing command: " + line);
                    e.printStackTrace();
                }
            }

            reader.close();
            system.output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}