public class Main {

    public static void main(String[] args) {

        String userName = getUserName(args);
        GitHubActivity gitHubActivity = new GitHubActivity(userName);
        gitHubActivity.printEvents();
    }

    private static String getUserName(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Main <GitHub username>");
            System.exit(1);
        }
        return args[0];
    }
}