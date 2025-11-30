-- Simple Queries
SELECT * FROM VENUE
WHERE CAPACITY >= 200;

SELECT * FROM TABLES
WHERE STATUS = 'Occupied';

SELECT * FROM DepartmentDirectory
WHERE contact_number = '9810012345';

SELECT * FROM Vendor
WHERE vendor_type = 'Beverage';

SELECT * FROM Sponsor
WHERE tier = 'Platinum';

SELECT * FROM Merchandise
WHERE price >= 1000;

SELECT * FROM Event
WHERE event_type = 'Managerial';

SELECT * FROM Participants
WHERE room_id = 78;

SELECT * FROM Participants
WHERE arrival_date = '2025-10-18';

SELECT * FROM Guests
WHERE fee >= 100000;

SELECT * FROM Ticketing
WHERE ticket_type = 'VIP';

SELECT * FROM VendorTransactions;

SELECT * FROM Volunteer
WHERE dept_id = 6;

SELECT * FROM Inventory
WHERE type = 'Audio';

SELECT * FROM Registration
WHERE registration_date BETWEEN '2025-10-11' AND '2025-10-16';

SELECT * FROM Score
WHERE score > 70;

SELECT * FROM EventManagement
WHERE volunteer_id = 8;

SELECT * FROM Guests
WHERE role = 'Artist';

SELECT * FROM Ticketing
WHERE status = 'Available';

SELECT * FROM Transactions
WHERE transaction_type IN ('Sponsor', 'Merchandise Sale');

-- Complex Queries
-- 1. Event name and total participants
SELECT e.event_name, COUNT(p.participant_id) AS total_participants
FROM Event e
LEFT JOIN Score s ON e.event_id = s.event_id
LEFT JOIN Participants p ON s.participant_id = p.participant_id
GROUP BY e.event_name;

-- 2. Out-of-station participants and room_id
SELECT p.name, a.room_id
FROM Participants p
JOIN Accommodation a ON p.participant_id = a.participant_id
WHERE p.location != 'In Station';

-- 3. Volunteers, department, and event managing
SELECT v.name AS volunteer_name, d.dept_name, e.event_name
FROM Volunteer v
JOIN Department d ON v.dept_id = d.dept_id
JOIN EventManagement em ON v.volunteer_id = em.volunteer_id
JOIN Event e ON em.event_id = e.event_id
ORDER BY d.dept_name;

-- 4. Events with no judges or artists
SELECT e.event_id, e.event_name
FROM Event e
LEFT JOIN EventManagement em ON e.event_id = em.event_id
WHERE em.judge_id IS NULL AND em.artist_id IS NULL;

-- 5. Attendees who bought tickets but not registered in any event
SELECT DISTINCT p.name
FROM Participants p
JOIN Ticketing t ON p.participant_id = t.participant_id
LEFT JOIN Score s ON p.participant_id = s.participant_id
WHERE s.event_id IS NULL;

-- 6. Judges and their average score for event 49
SELECT g.name AS judge_name, AVG(s.score) AS avg_score
FROM Guests g
JOIN Score s ON g.guest_id = s.judge_id
WHERE s.event_id = 49
GROUP BY g.name;

-- 7. Events whose prize money is greater than average prize money
SELECT event_name, prize_money
FROM Event
WHERE prize_money > (SELECT AVG(prize_money) FROM Event);

-- 8. Top 3 highest-scoring participants for event 38
SELECT p.name, AVG(s.score) AS avg_score
FROM Participants p
JOIN Score s ON p.participant_id = s.participant_id
WHERE s.event_id = 38
GROUP BY p.name
ORDER BY avg_score DESC
LIMIT 3;

-- 9. Participant names and venues
SELECT p.name, v.venue_name
FROM Participants p
JOIN Score s ON p.participant_id = s.participant_id
JOIN Event e ON s.event_id = e.event_id
JOIN Venue v ON e.venue_id = v.venue_id
ORDER BY e.event_name;

-- 10. Function to calculate total event cost
CREATE OR REPLACE FUNCTION total_event_cost(ev INT)
RETURNS NUMERIC AS $$
DECLARE total NUMERIC;
BEGIN
    SELECT (e.prize_money + COALESCE(SUM(g.fee), 0)) INTO total
    FROM Event e
    LEFT JOIN Guests g ON e.event_id = g.event_assigned
    WHERE e.event_id = ev
    GROUP BY e.prize_money;
    RETURN total;
END;
$$ LANGUAGE plpgsql;

-- 11. Function to count volunteers in department
CREATE OR REPLACE FUNCTION count_volunteers(dep_name VARCHAR)
RETURNS INT AS $$
DECLARE total INT;
BEGIN
    SELECT COUNT(*) INTO total
    FROM Volunteer
    WHERE dept_name = dep_name;
    RETURN total;
END;
$$ LANGUAGE plpgsql;

-- 12. Participants registered in Main Auditorium
SELECT DISTINCT p.name
FROM Participants p
JOIN Score s ON p.participant_id = s.participant_id
JOIN Event e ON s.event_id = e.event_id
WHERE e.venue_name = 'Main Auditorium';

-- 13. Department with highest inventory items
SELECT d.dept_name
FROM Department d
JOIN Inventory i ON d.dept_id = i.dept_id
GROUP BY d.dept_name
ORDER BY COUNT(i.item_id) DESC
LIMIT 1;

-- 14. Sponsors that contributed more than any Associate tier sponsor
SELECT s.name, s.tier, s.amount
FROM Sponsor s
WHERE s.amount > ALL (SELECT amount FROM Sponsor WHERE tier = 'Associate');

-- 15. Function to count unique participants by event type
CREATE OR REPLACE FUNCTION unique_participants_by_event_type(eventType VARCHAR)
RETURNS INT AS $$
DECLARE total INT;
BEGIN
    SELECT COUNT(DISTINCT s.participant_id) INTO total
    FROM Score s
    JOIN Event e ON s.event_id = e.event_id
    WHERE e.event_type = eventType;
    RETURN total;
END;
$$ LANGUAGE plpgsql;

-- 16. Function to calculate merchandise revenue
CREATE OR REPLACE FUNCTION total_merchandise_revenue(itemName VARCHAR)
RETURNS NUMERIC AS $$
DECLARE revenue NUMERIC;
BEGIN
    SELECT SUM(amount) INTO revenue
    FROM Transactions
    WHERE merchandise_name = itemName;
    RETURN COALESCE(revenue, 0);
END;
$$ LANGUAGE plpgsql;

-- 17. Function to check if venue is free at timestamp
CREATE OR REPLACE FUNCTION is_venue_free(venueId INT, eventTimestamp TIMESTAMP)
RETURNS BOOLEAN AS $$
DECLARE booked INT;
BEGIN
    SELECT COUNT(*) INTO booked
    FROM Event
    WHERE venue_id = venueId
    AND event_time = eventTimestamp;
    RETURN booked = 0;
END;
$$ LANGUAGE plpgsql;

-- 18. Trigger to decrease merchandise quantity after sale
CREATE OR REPLACE FUNCTION update_merchandise_stock()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Merchandise
    SET quantity_available = quantity_available - NEW.quantity_sold
    WHERE merchandise_id = NEW.merchandise_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER decrease_merchandise_stock
AFTER INSERT ON MerchandiseSales
FOR EACH ROW
EXECUTE FUNCTION update_merchandise_stock();

-- 19. Trigger to prevent deletion of Title tier sponsors
CREATE OR REPLACE FUNCTION prevent_title_sponsor_delete()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.tier = 'Title' THEN
        RAISE EXCEPTION 'Title tier sponsors cannot be deleted!';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER title_sponsor_delete_protection
BEFORE DELETE ON Sponsor
FOR EACH ROW
EXECUTE FUNCTION prevent_title_sponsor_delete();

-- 20. Top 5 expensive events by cost per participant
WITH EventCosts AS (
    SELECT e.event_id, e.event_name,
           e.prize_money + COALESCE(SUM(g.fee), 0) AS total_cost,
           COUNT(s.participant_id) AS num_participants
    FROM Event e
    LEFT JOIN Guests g ON e.event_id = g.event_assigned
    LEFT JOIN Score s ON e.event_id = s.event_id
    GROUP BY e.event_id, e.event_name, e.prize_money
)
SELECT event_name, total_cost / NULLIF(num_participants, 0) AS cost_per_participant
FROM EventCosts
ORDER BY cost_per_participant DESC
LIMIT 5;
