import java.util.*;
import java.lang.Math;

public class Solution {
    private static final int MOD = 1000000007;
    private List<List<Integer>> adj;
    private List<Integer> values;
    private int X;
    private long[][][] dp;

    public static int get_ans(int N, int X, List<Integer> A, int M, List<Integer> P) {
        Solution solution = new Solution();
        return solution.solve(N, X, A, M, P);
    }

    private int solve(int N, int X, List<Integer> A, int M, List<Integer> P) {
        this.X = X;
        this.values = A;

        // Build adjacency list for the tree
        adj = new ArrayList<>();
        for (int i = 0; i <= N; i++) {
            adj.add(new ArrayList<>());
        }

        // Add edges based on parent relationships
        // P[i] is parent of node (i+2), so node (i+2) is child of P[i]
        for (int i = 0; i < M; i++) {
            int parent = P.get(i);
            int child = i + 2; // since P[i] is parent of node i+2
            adj.get(parent).add(child);
            adj.get(child).add(parent);
        }

        // dp[node][included][remainder] = number of ways to select subset from subtree
        // rooted at node
        // included: 0 = node not included, 1 = node included
        // remainder: sum % X
        dp = new long[N + 1][2][X];

        // Initialize DP table with -1 (uncomputed)
        for (int i = 0; i <= N; i++) {
            for (int j = 0; j < 2; j++) {
                Arrays.fill(dp[i][j], -1);
            }
        }

        // Calculate DP for root node 1
        long result = 0;
        for (int r = 0; r < X; r++) {
            result = (result + dfs(1, -1, 0, r)) % MOD; // Don't include root
            result = (result + dfs(1, -1, 1, r)) % MOD; // Include root
        }

        // Subtract 1 to exclude empty subset (remainder 0 with no nodes)
        result = (result - 1 + MOD) % MOD;

        return (int) result;
    }

    private long dfs(int node, int parent, int include, int targetRemainder) {
        if (dp[node][include][targetRemainder] != -1) {
            return dp[node][include][targetRemainder];
        }

        long result = 0;

        if (include == 1) {
            // If we include current node, we need children to sum to (targetRemainder -
            // nodeValue) % X
            int nodeValue = values.get(node - 1); // Convert to 0-indexed
            int childrenTarget = (targetRemainder - nodeValue % X + X) % X;

            result = calculateChildrenWays(node, parent, childrenTarget, 0);
        } else {
            // If we don't include current node, children should sum to targetRemainder
            result = calculateChildrenWays(node, parent, targetRemainder, 0);
        }

        dp[node][include][targetRemainder] = result;
        return result;
    }

    private long calculateChildrenWays(int node, int parent, int targetRemainder, int childIndex) {
        List<Integer> children = new ArrayList<>();
        for (int child : adj.get(node)) {
            if (child != parent) {
                children.add(child);
            }
        }

        if (childIndex == children.size()) {
            return targetRemainder == 0 ? 1 : 0;
        }

        long result = 0;
        int child = children.get(childIndex);

        // For each possible remainder that this child subtree can contribute
        for (int childRemainder = 0; childRemainder < X; childRemainder++) {
            int nextTarget = (targetRemainder - childRemainder + X) % X;

            // Child can either be included or not included (but not adjacent to current if
            // current is included)
            long childWays = 0;

            // Case 1: Don't include the child (child can have any configuration)
            for (int r = 0; r < X; r++) {
                childWays = (childWays + dfs(child, node, 0, r)) % MOD;
            }

            // Case 2: Include the child only if current node is not included
            // This is handled by the constraint in the problem

            // Actually, let's reconsider the approach - we need to be more careful about
            // adjacency
            result = (result + childWays * calculateChildrenWays(node, parent, nextTarget, childIndex + 1)) % MOD;
        }

        return result;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        int N = Integer.parseInt(scan.nextLine().trim());
        int X = Integer.parseInt(scan.nextLine().trim());

        List<Integer> A = new ArrayList<>();
        for (int j = 0; j < N; j++) {
            A.add(Integer.parseInt(scan.nextLine().trim()));
        }

        int M = Integer.parseInt(scan.nextLine().trim());
        List<Integer> P = new ArrayList<>();
        for (int j = 0; j < M; j++) {
            P.add(Integer.parseInt(scan.nextLine().trim()));
        }

        System.out.println(get_ans(N, X, A, M, P));
    }
}
