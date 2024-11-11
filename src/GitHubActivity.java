public class GitHubActivity {

    public static void main(String[] args) {

        String userName = getUserName(args);
        GitHubService gitHubService = new GitHubService(userName);
        gitHubService.printEvents();
    }

    private static String getUserName(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Main <GitHub username>");
            System.exit(1);
        }
        return args[0];
    }
}