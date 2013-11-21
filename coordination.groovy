/**
 * connects to a the database and gets details of  all of the users for a project
 * 
 * @author Kelly Blincoe
 */

import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import net.wagstrom.research.github.IndexNames
import net.wagstrom.research.github.IdCols
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PullRequest {
       public Vertex vertex;
       public ArrayList issues;
       public Vertex owner;
       public boolean first;
       public Vertex merger;

       public PullRequest (Vertex v){
           vertex = v;
           issues = new ArrayList<String>();
	   first = false;
       }
}

public class Comment {
       public Vertex vertex;
       public Vertex owner;

       public Comment (Vertex v){
           vertex = v;
       }
}

public class Issue {
       public Vertex vertex;
       public Vertex owner;
       public ArrayList comments;
       public ArrayList commentOwners;
       public ArrayList votes;
       public ArrayList voters;

       public Issue(Vertex v){
           vertex = v;
	   comments = new ArrayList<Vertex>();
	   votes = new ArrayList<Vertex>();
	   commentOwners = new ArrayList<Vertex>();
	   voters = new ArrayList<Vertex>();
       }

       public addComment(Comment comment){
           if(comment.vertex.body.contains("+1")){
		votes.add(comment);
		    if(!voters.contains(comment.owner)){
			voters.add(comment.owner);
		    }
	   }else{
		comments.add(comment);
		if(!commentOwners.contains(comment.owner)){
		    commentOwners.add(comment.owner);
		}
	   }
       }
}

def getPullRequestOwners(String reponame, Graph g){
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).iterator().next()
    pullRequestOwners = new HashSet()

    pullRequestOwners = Helpers.getRepositoryPullRequestOwners(repo).login.toList()
    mergedPullRequestOwners = Helpers.getRepositoryMergedPullRequestOwners(repo).login.toList()

    pullRequestOwners.addAll(pullRequestOwners)
    pullRequestOwners.addAll(mergedPullRequestOwners)

    return pullRequestOwners.toList()
}

def getPullRequests(String reponame, Graph g, List pullRequestOwners) {
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).iterator().next()
    pullRequests = new HashSet()

    for(owner in pullRequestOwners){
        owner = g.idx(IndexNames.USER).get(IdCols.USER, owner).iterator().next()
	ownerPullRequests = owner.out(EdgeType.PULLREQUESTOWNER).dedup()
    	Date firstPRThisRepoDate = new Date();
    	PullRequest firstPRThisRepo = null;
	Integer count = 0;
    	for (pr in ownerPullRequests) {
	    PullRequest newPr = new PullRequest(pr);
	    newPr.owner = owner;
	    pullRequests.add(newPr);
            Date thisPRDate = new Date((long)pr.createdAt*1000);
            if (pr.pullrequest_id.contains(reponame)){
                count++
           	if(thisPRDate.before(firstPRThisRepoDate)){
		    firstPRThisRepoDate = thisPRDate;
		    firstPRThisRepo = newPr;
           	}
      	    }
    	}
    	firstPRThisRepoDate = (count > 0) ? firstPRThisRepoDate : null;

    	if(firstPRThisRepoDate != null){
	    firstPRThisRepo.first = true;
	}
    }

    return pullRequests.toList()
}

def getMergedPullRequests(String reponame, Graph g){
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).iterator().next()
    mergedPullRequests = new HashSet()

    mergedPullRequests = Helpers.getRepositoryMergedPullRequests(repo).toList()

    return mergedPullRequests.toList()

}

def getIssues(String reponame, Graph g){
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).iterator().next()
    issueOwners = new HashSet()
    issues = new HashMap<Integer,Issue>() 

    issueOwners = Helpers.getRepositoryIssueOwners(repo).login.toList()
    for(ownerName in issueOwners){
        owner = g.idx(IndexNames.USER).get(IdCols.USER, ownerName).iterator().next()
	ownerIssues = owner.out(EdgeType.ISSUEOWNER).dedup()
	for(issue in ownerIssues){
	    Issue newIssue = new Issue(issue);
	    newIssue.owner = owner;
	    Integer num = Integer.valueOf(issue.number);
	    issues.put(num, newIssue);
	}
    }

    return issues
}

def getIssueComments(String reponame, Graph g, HashMap<Integer, Issue> issues){
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).iterator().next()

    pathList = repo.out('ISSUE').out('ISSUE_COMMENT').in('ISSUE_COMMENT_OWNER').path.toSet()
    for (path in pathList){
        issue = path.get(1)
	comment = path.get(2)
	owner = path.get(3)
	Issue thisIssue = issues.get(Integer.valueOf(issue.number));
	Comment thisComment = new Comment(comment);
	thisComment.owner = owner;
	thisIssue.addComment(thisComment);
    }
}

def getPullRequestMergers(String reponame, Graph g, HashMap<Integer,PullRequest> pullRequests){
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).iterator().next()

    pathList = repo.out(EdgeType.PULLREQUEST).out('PULLREQUEST_MERGED_BY').path.toSet()
    for (path in pathList){
        pr = path.get(1)
        merger = path.get(2)
        PullRequest thisPr = pullRequests.get(Integer.valueOf(pr.number));
        thisPr.merger = merger;
    }
}

def linkPullRequestsToIssues(String reponame, List pullRequests){

    linkedPullRequests = new HashMap<Integer,PullRequest>()
    for(pr in pullRequests){
        ArrayList issues = new ArrayList<String>();
	if(pr.vertex.body != null){
	    issues.addAll(getPullRequestIssues(reponame, pr.vertex.body));
	}
	if(pr.vertex.title != null){
	    issues.addAll(getPullRequestIssues(reponame, pr.vertex.title));
	}
	pr.issues = issues;
	linkedPullRequests.put(Integer.valueOf(pr.vertex.number), pr)
    }

    return linkedPullRequests
}

def getPullRequestIssues(String repoName, String searchText){
    ArrayList issues = new ArrayList<String>();

    Pattern issueLink = Pattern.compile("https://github.com/" + repoName + "/issues/" + "([0-9]+)");
    Matcher mIssueLink = issueLink.matcher(searchText);
    if(mIssueLink.find()){
	for(int i = 1; i <= mIssueLink.groupCount(); i++){
	    if(!issues.contains(mIssueLink.group(i))){
	        issues.add(mIssueLink.group(i));
	    }	
	}
    }

    Pattern closes = Pattern.compile("[Cc]loses #*" + "([0-9]+)");
    Matcher mCloses = closes.matcher(searchText);
    if(mCloses.find()){
        for(int i = 1; i<= mCloses.groupCount(); i++){
	    if(!issues.contains(mCloses.group(i))){
                issues.add(mCloses.group(i));
            }
	}
    }

    Pattern fixes = Pattern.compile("[Ff]ixes #*" + "([0-9]+)");
    Matcher mFixes = fixes.matcher(searchText);
    if(mFixes.find()){
        for(int i = 1; i<= mFixes.groupCount(); i++){
	    if(!issues.contains(mFixes.group(i))){
                issues.add(mFixes.group(i));
            }
	}
    }

    Pattern fix = Pattern.compile("[Ff]ix #*" + "([0-9]+)");
    Matcher mFix = fix.matcher(searchText);
    if(mFix.find()){
        for(int i = 1; i<= mFix.groupCount(); i++){
	    if(!issues.contains(mFix.group(i))){
                issues.add(mFix.group(i));
            }
        }
    }

    Pattern issue = Pattern.compile("[Ii]ssue #*" + "([0-9]+)");
    Matcher mIssue = issue.matcher(searchText);
    if(mIssue.find()){
        for(int i = 1; i<= mIssue.groupCount(); i++){
            if(!issues.contains(mIssue.group(i))){
                issues.add(mIssue.group(i));
            }
        }
    }

    Pattern gh = Pattern.compile("[Gg][Hh] #*" + "([0-9]+)");
    Matcher mGh = gh.matcher(searchText);
    if(mGh.find()){
        for(int i = 1; i<= mGh.groupCount(); i++){
	    if(!issues.contains(mGh.group(i))){
                issues.add(mGh.group(i));
            }
        }
    }

    return issues
}

def printPullReqestInfo(HashMap<Integer,PullRequest> allPullRequests, List mergedPullRequests, HashMap<Integer,Issue> issues){
    System.out.println("pr_number,pr_owner,first_pr,pr_commits,pr_comments,merged,linked,sameOwner,comments,commentsB4,ownerComments,ownerCommentsB4,mergerComments,mergerCommentsB4");
    for(prNum in allPullRequests.keySet()){
        PullRequest pr = allPullRequests.get(prNum)
        boolean linked = (pr.issues.size() > 0) ? true : false;
	boolean merged = (mergedPullRequests.contains(pr.vertex)) ? true : false;
	boolean sameOwner = false;

    	String prString = pr.vertex.number + ",";
	prString += pr.owner.login + ",";
	prString += (pr.first) ? "1," : "0,";
        prString += pr.vertex.commits + ",";
	prString += pr.vertex.comments + ",";
	prString += (merged) ? "1," : "0,";
	prString += (linked) ? "1," : "0,";

	if(linked){
	    int ownerComments = 0;
	    int ownerCommentsB4 = 0;
	    int comments = 0;
	    int commentsB4 = 0;
	    int mergerComments = 0;
	    int mergerCommentsB4 = 0;

	    for(issueNum in pr.issues){
	        Integer num = Integer.valueOf(issueNum);
	        if(issues.containsKey(num)){
		    Issue issue = issues.get(num);
	            if(issue.owner.login.equals(pr.owner.login)){
	    	        sameOwner = true;
	    	    }
                    for(Comment comment : issue.comments){
                        comments++;
                        if(comment.owner.equals(pr.owner)){
                            ownerComments++;
                        }
			if(merged && comment.owner.equals(pr.merger)){
			    mergerComments++;
			}
			Date prDate = new Date((long)pr.vertex.createdAt*1000);
			Date commentDate = new Date((long)comment.vertex.createdAt*1000);
                        if(commentDate.before(prDate)){
                            commentsB4++;
			    if(comment.owner.equals(pr.owner)){
                                ownerCommentsB4++;
                            }
			    if(merged && comment.owner.equals(pr.merger)){
			        mergerCommentsB4++;
                            }
                        }
		    }
	    	}
	    }	
	    prString += (sameOwner) ? "1," : "0,";
	    prString += Integer.toString(comments) + ",";
	    prString += Integer.toString(commentsB4) + ",";
            prString +=	Integer.toString(ownerComments) + ",";
            prString +=	Integer.toString(ownerCommentsB4) + ",";
	    if(merged){
	        prString += Integer.toString(mergerComments) + ",";
		prString += Integer.toString(mergerCommentsB4);
	    }else{
	        prString += ",";
	    }
	}else{
	    prString += ",,,,,,";
	}

	System.out.println(prString);
    }
}

g = new Neo4jGraph("/home/kellyb/new_data/graph.db")
repos = ["saltstack/salt"]
for (reponame in repos){
    pullRequestOwners = getPullRequestOwners(reponame, g)
    pullRequests = getPullRequests(reponame, g, pullRequestOwners)
    mergedPullRequests = getMergedPullRequests(reponame, g)
    issues = getIssues(reponame, g)
    getIssueComments(reponame, g, issues)
    allPullRequests = linkPullRequestsToIssues(reponame, pullRequests)
    getPullRequestMergers(reponame, g, allPullRequests)
    printPullReqestInfo(allPullRequests, mergedPullRequests, issues)
    
}
g.shutdown()
