import java.util.*;

class BookNotAvailableException extends Exception
{
    public BookNotAvailableException(String message)
    {
        super(message);
    }
}
//Helps with errors and throws an exception when a book is checked out
class BookNotFoundException extends Exception
{
    public BookNotFoundException(String message)
    {
        super(message);
    }
}
// This class keeps track and knows when a book has been checked out or has been retuned
class Book
{
    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;

    public Book(String title, String author, String genre)
    {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isAvailable = true;
    }

    public boolean isAvailable()
    {
        return isAvailable;
    }

    public void checkout() throws BookNotAvailableException
    {
        if (!isAvailable)
        {
            throw new BookNotAvailableException("The book \"" + title + "\" is already checked out.");
        }
        isAvailable = false;
    }

    public void forceCheckout()
    {
        isAvailable = false;
    }

    public void returnBook()
    {
        isAvailable = true;
    }

    public String getGenre()
    {
        return genre;
    }

    public String getTitle()
    {
        return title;
    }

    public String toString()
    {
        return "\"" + title + "\" by " + author + " (" + genre + ") " + (isAvailable ? "[Available]" : "[Checked Out]");
    }
}

interface CheckoutStrategy
{
    void check(Book book) throws BookNotAvailableException;
}
//This is for when users say they are regular users.
class RegularUserStrategy implements CheckoutStrategy
{
    public void check(Book book) throws BookNotAvailableException
    {
        book.checkout();
    }
}
//This is for when users say they are premium users, They both do the same thing right now but in the future I could add on to these codes
//and make premium users have perks. 
class PremiumUserStrategy implements CheckoutStrategy
{
    public void check(Book book) throws BookNotAvailableException
    {
       book.checkout();
    }
}

//This stores all the books in the Array and makes sure that books are available
class LibraryCollection implements Iterable<Book>
{
    private Map<String, List<Book>> genreMap;

    public LibraryCollection()
    {
        genreMap = new HashMap<>();
    }

    public void addBook(Book book)
    {
        genreMap.computeIfAbsent(book.getGenre(), k -> new ArrayList<>()).add(book);
    }

    public Iterator<Book> getGenreIterator(String genre)
    {
        List<Book> books = genreMap.getOrDefault(genre, Collections.emptyList());
        return new AvailableBookIterator(books);
    }

    public Book findBookByTitle(String title) throws BookNotFoundException
    {
        for (List<Book> books : genreMap.values())
        {
            for (Book book : books)
            {
                if (book.getTitle().equalsIgnoreCase(title))
                {
                    return book;
                }
            }
        }
        throw new BookNotFoundException("Book titled \"" + title + "\" not found.");
    }
    public Iterator<Book> iterator()
    {
        List<Book> allBooks = new ArrayList<>();
        for (List<Book> list : genreMap.values())
        {
            allBooks.addAll(list);
        }
        return allBooks.iterator();
    }

    private class AvailableBookIterator implements Iterator<Book>
    {
        private List<Book> books;
        private int currentIndex = 0;

        public AvailableBookIterator(List<Book> books)
        {
            this.books = books;
        }

        public boolean hasNext()
        {
            while (currentIndex < books.size())
            {
                if (books.get(currentIndex).isAvailable())
                {
                    return true;
                }
                currentIndex++;
            }
            return false;
        }

        public Book next()
        {
            if (!hasNext()) throw new NoSuchElementException();
            return books.get(currentIndex++);
        }
    }
}
//This class uses checkoutstrategy to see how they check out books, basically represents a user of the library
//Also makes changing code easier
class LibraryUser
{
    private String name;
    private CheckoutStrategy strategy;

    public LibraryUser(String name, CheckoutStrategy strategy)
    {
        this.name = name;
        this.strategy = strategy;
    }

    public void borrowBook(Book book) throws BookNotAvailableException
    {
        strategy.check(book);
    }

    public void returnBook(Book book)
    {
        book.returnBook();
    }

    public String getName()
    {
        return name;
    }
}
//Sets up the whole test of the system.
public class LibraryTest
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        LibraryCollection library = new LibraryCollection();

        library.addBook(new Book("How to code", "Kai Johnson", "Fiction"));
        library.addBook(new Book("Rats Rats Rats", "Ezry Roukema", "Fantasy"));
        library.addBook(new Book("Musicals", "Alex Hamilton", "Non-Fiction"));
        library.addBook(new Book("Video Game", "Dylan Noriega", "Sci-Fi"));
        library.addBook(new Book("How to be a surfer", "Justin Ferrari", "Non-Fiction"));

        System.out.print("Are you a premium user? (yes/no): ");
        String userType = scanner.nextLine();
        CheckoutStrategy strategy = userType.equalsIgnoreCase("yes")
            ? new PremiumUserStrategy()
            : new RegularUserStrategy();

        while (true)
        {
            System.out.print("\nMenu:\n1. View available books\n2. Checkout a book\n3. Return a book\n4. Exit");
            System.out.print("\nChoose an option: ");
            String option = scanner.nextLine();

            if (option.equals("1"))
            {
                System.out.print("Enter genre to browse: ");
                String genre = scanner.nextLine();
                Iterator<Book> it = library.getGenreIterator(genre);
                boolean found = false;
                System.out.println("Available books in genre '" + genre + "':");
                while (it.hasNext())
                {
                    System.out.println(" - " + it.next());
                    found = true;
                }
                if (!found){
                    System.out.println("No available books in this genre.");
                }
            } else if (option.equals("2")){
                System.out.print("Enter book title to checkout: ");
                String checkoutTitle = scanner.nextLine();
                try {
                    Book bookToCheckout = library.findBookByTitle(checkoutTitle);
                    strategy.check(bookToCheckout);
                    System.out.println("Successfully checked out: " + bookToCheckout);
                } catch (BookNotFoundException | BookNotAvailableException e) {
                    System.out.println(e.getMessage());
                }
            } else if (option.equals("3")){
                System.out.print("Enter book title to return: ");
                String returnTitle = scanner.nextLine();
                try {
                    Book bookToReturn = library.findBookByTitle(returnTitle);
                    bookToReturn.returnBook();
                    System.out.println("Returned: " + bookToReturn);
                } catch (BookNotFoundException e){
                    System.out.println(e.getMessage());
                }
            } else if (option.equals("4")){
                System.out.println("Thanks for visiting the library.");
                break;
            } else {
                System.out.println("Invalid option.");
            }
            
        }

        scanner.close();
    }
}
