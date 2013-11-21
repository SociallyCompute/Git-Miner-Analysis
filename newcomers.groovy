/**
 * connects to a the database and gets details of  all of the users for a project
 * 
 * @author Kelly Blincoe
 */

import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import net.wagstrom.research.github.IndexNames
import net.wagstrom.research.github.IdCols

def dumpRepositoryUsers(String reponame, Graph g) {
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).iterator().next()
    allUsers = new HashSet()
    issueUsers = new HashSet()
    pullRequestUsers = new HashSet()
    
    contributors = Helpers.getRepositoryContributors(repo).login.toList()
    issueOwners = Helpers.getRepositoryIssueOwners(repo).login.toList()
    issueCommenters = Helpers.getRepositoryIssueCommenters(repo).login.toList()
    issueClosers = Helpers.getRepositoryIssueClosers(repo).login.toList()
    issueSubscribers = Helpers.getRepositoryIssueSubscribers(repo).login.toList()

    issueUsers.addAll(issueOwners)
    issueUsers.addAll(issueCommenters)
    issueUsers.addAll(issueClosers)
    issueUsers.addAll(issueSubscribers)

    pullRequestOwners = Helpers.getRepositoryPullRequestOwners(repo).login.toList()
    mergedPullRequestOwners = Helpers.getRepositoryMergedPullRequestOwners(repo).login.toList()
    pullRequestMergers = Helpers.getRepositoryPullRequestMergers(repo).login.toList()

    pullRequestUsers.addAll(pullRequestOwners)
    pullRequestUsers.addAll(mergedPullRequestOwners)
    pullRequestUsers.addAll(pullRequestMergers)

    forkOwners = Helpers.getRepositoryForkOwners(repo).login.toList()

    committers = Helpers.getRepositoryCommitters(repo).login.toList()

    allUsers.addAll(issueUsers)
    allUsers.addAll(pullRequestUsers)
    allUsers.addAll(contributors)
    allUsers.addAll(forkOwners)
    allUsers.addAll(committers)

    println "login,issuesCreatedThisRepoCount,issuesCreatedCount,firstIssueThisRepo,firstIssue,lastIssueThisRepo,lastIssue,issuesAssignedThisRepoCount,issuesAssignedCount,firstIssueAssignedThisRepo,firstIssueAssigned,lastIssueAssignedThisRepo,lastIssueAssigned,commitThisRepoCount,commitCount,firstCommitThisRepo,firstCommit,lastCommitThisRepo,lastCommit,prThisRepoCount,prCount,mergedPRThisRepoCount,mergedPRCount,firstPRThisRepo,firstPR,lastPRThisRepo,lastPR,commentsThisRepoCount,commentsCount,firstCommentThisRepo,firstComment,lastCommentThisRepo,lastComment"
    allUsers.each{userDetails(it,reponame,g)}    

}

def userDetails(String login, String reponame, Graph g){
    user = g.idx(IndexNames.USER).get(IdCols.USER, login).iterator().next()

    //todo print this information in final println statement if you want it
    repoWatchedCount = user.out(EdgeType.REPOWATCHED).dedup().count()
    repoOwnerCount = user.out(EdgeType.REPOOWNER).dedup().count()
    organizationCount = user.out(EdgeType.ORGANIZATIONMEMBER).dedup().count()
    followersCount = user.out(EdgeType.FOLLOWER).dedup().count()
    followingCount = user.out(EdgeType.FOLLOWING).dedup().count()
    email = user.out(EdgeType.EMAIL).dedup()

    issuesCreated = user.out(EdgeType.ISSUEOWNER).dedup();
    Date firstIssue = new Date();
    Date firstIssueThisRepo = new Date();
    Date lastIssue = new Date(1);
    Date lastIssueThisRepo = new Date(1);
    Integer issuesCreatedCount = 0;
    Integer issuesCreatedThisRepoCount = 0;
    for (issue in issuesCreated) {
        Date thisIssue = new Date((long)issue.createdAt*1000);
	issuesCreatedCount++;
        if (thisIssue.before(firstIssue)) {
            firstIssue = thisIssue;
	}
	if (thisIssue.after(lastIssue)){
	   lastIssue = thisIssue;
	}
	if (issue.issue_id.contains(reponame)){
	   issuesCreatedThisRepoCount++;
	   if(thisIssue.before(firstIssueThisRepo)){
		firstIssueThisRepo = thisIssue;
           }
	   if(thisIssue.after(lastIssueThisRepo)){
		lastIssueThisRepo = thisIssue;
	   }
 	}
    }
    firstIssueThisRepo = (issuesCreatedThisRepoCount > 0) ? firstIssueThisRepo : null;
    lastIssueThisRepo = (issuesCreatedThisRepoCount > 0) ? lastIssueThisRepo : null;

    issuesAssigned = user.out(EdgeType.ISSUEASSIGNEE).dedup()
    Date firstIssueAssigned = new Date();
    Date firstIssueAssignedThisRepo = new Date();
    Date lastIssueAssigned = new Date(1);
    Date lastIssueAssignedThisRepo = new Date(1);
    Integer issuesAssignedCount = 0;
    Integer issuesAssignedThisRepoCount = 0;
    for (issue in issuesAssigned) {
        Date thisIssue = new Date((long)issue.createdAt*1000);
        issuesAssignedCount++;
 	if (thisIssue.before(firstIssueAssigned)) {
            firstIssueAssigned = thisIssue
        }
        if (thisIssue.after(lastIssueAssigned)){
           lastIssueAssigned = thisIssue;
        }
	if (issue.issue_id.contains(reponame)){
           issuesAssignedThisRepoCount++;
           if(thisIssue.before(firstIssueAssignedThisRepo)){
                firstIssueAssignedThisRepo = thisIssue;
           }
           if(thisIssue.after(lastIssueAssignedThisRepo)){
                lastIssueAssignedThisRepo = thisIssue;
           }
        }
    }
    firstIssueAssignedThisRepo = (issuesAssignedThisRepoCount > 0) ? firstIssueAssignedThisRepo : null;
    lastIssueAssignedThisRepo = (issuesAssignedThisRepoCount > 0) ? lastIssueAssignedThisRepo : null;

    commits = user.out(EdgeType.ISSUEEVENTACTOR).out(EdgeType.EVENTCOMMIT).dedup()
    Date firstCommit = new Date();
    Date firstCommitThisRepo = new Date();
    Date lastCommit = new Date(1);
    Date lastCommitThisRepo = new Date(1);
    Integer commitCount = 0;
    Integer commitThisRepoCount = 0;
    for (commit in commits) {
        Date thisCommit = new Date((long)commit.sys_created_at*1000);
	commitCount++;
        if (thisCommit.before(firstCommit)) {
            firstCommit = thisCommit
        }
        if (thisCommit.after(lastCommit)){
           lastCommit = thisCommit;
	}
        //todo decide how we want to track commits with null url, I think these are commits that have since been deleted
	if (commit.url?.contains(reponame)){
	   commitThisRepoCount++;
           if(thisCommit.before(firstCommitThisRepo)){
              firstCommitThisRepo = thisCommit;
           }
	   if(thisCommit.after(lastCommitThisRepo)){
               lastCommitThisRepo = thisCommit;
           }
        }
    }
    firstCommitThisRepo = (commitThisRepoCount > 0) ? firstCommitThisRepo : null;
    lastCommitThisRepo = (commitThisRepoCount > 0) ? lastCommitThisRepo : null;

    pullRequests = user.out(EdgeType.PULLREQUESTOWNER).dedup()
    Date firstPR = new Date();    
    Date firstPRThisRepo = new Date();
    Date lastPR = new Date(1);
    Date lastPRThisRepo = new Date(1);
    Integer prCount = 0;
    Integer prThisRepoCount = 0;
    for (pr in pullRequests) {
    	Date thisPR = new Date((long)pr.createdAt*1000);
	prCount++;
	if (thisPR.before(firstPR)) {
            firstPR = thisPR
        }
        if (thisPR.after(lastPR)){
           lastPR = thisPR;
        }
	if (pr.pullrequest_id.contains(reponame)){
           prThisRepoCount++;
           if(thisPR.before(firstPRThisRepo)){
                firstPRThisRepo = thisPR;
           }
           if(thisPR.after(lastPRThisRepo)){
                lastPRThisRepo = thisPR;
           }
        }
    }
    firstPRThisRepo = (prThisRepoCount > 0) ? firstPRThisRepo : null;
    lastPRThisRepo = (prThisRepoCount > 0) ? lastPRThisRepo : null;

    mergedPullRequests = user.out(EdgeType.PULLREQUESTOWNER).dedup().out(EdgeType.PULLREQUESTMERGEDBY).path.toSet()
    Integer mergedPRCount = 0;
    Integer mergedPRThisRepoCount = 0;
    for (path in mergedPullRequests){
    	mergedPRCount++;
	if(path[1].pullrequest_id.contains(reponame)){
	    mergedPRThisRepoCount++;
	}
    }

    comments = user.out(EdgeType.ISSUECOMMENTOWNER).dedup()
    Date firstComment = new Date();
    Date firstCommentThisRepo = new Date();
    Date lastComment = new Date(1);
    Date lastCommentThisRepo = new Date(1);
    Integer commentsCount = 0;
    Integer commentsThisRepoCount = 0;
    for (comment in comments) {
        Date thisComment = new Date((long)comment.createdAt*1000);
        commentsCount++;
	if (thisComment.before(firstComment)) {
            firstComment = thisComment
        }
        if (thisComment.after(lastComment)){
           lastComment = thisComment;
        }
	if (comment.url.contains(reponame)){
           commentsThisRepoCount++;
           if(thisComment.before(firstCommentThisRepo)){
                firstCommentThisRepo = thisComment;
           }
           if(thisComment.after(lastCommentThisRepo)){
                lastCommentThisRepo = thisComment;
           }
        }
    }
    comments = user.out(EdgeType.PULLREQUESTCOMMENTOWNER).dedup()
    for (comment in comments) {
    	Date thisComment = new Date((long)comment.createdAt*1000);
        commentsCount++;
        if (thisComment.before(firstComment)) {
            firstComment = thisComment
        }
        if (thisComment.after(lastComment)){
           lastComment = thisComment;
        }
        if (comment.url.contains(reponame)){
           commentsThisRepoCount++;
           if(thisComment.before(firstCommentThisRepo)){
                firstCommentThisRepo = thisComment;
           }
           if(thisComment.after(lastCommentThisRepo)){
                lastCommentThisRepo = thisComment;
           }
        }
    }
    comments = user.out(EdgeType.COMMITCOMMENTOWNER).dedup()
    for (comment in comments) {
        Date thisComment = new Date((long)comment.createdAt*1000);
	commentsCount++;
        if (thisComment.before(firstComment)) {
            firstComment = thisComment
        }
        if (thisComment.after(lastComment)){
           lastComment = thisComment;
        }
        if (comment.url.contains(reponame)){
           commentsThisRepoCount++;
	      if(thisComment.before(firstCommentThisRepo)){
                firstCommentThisRepo = thisComment;
		   }
           if(thisComment.after(lastCommentThisRepo)){
                lastCommentThisRepo = thisComment;
           }
        }
    }    
    firstCommentThisRepo = (commentsThisRepoCount > 0) ? firstCommentThisRepo : null;    
    lastCommentThisRepo = (commentsThisRepoCount > 0) ? lastCommentThisRepo : null;

    println login + "," + issuesCreatedThisRepoCount + "," + issuesCreatedCount + "," + firstIssueThisRepo + "," + firstIssue + "," + lastIssueThisRepo + "," + lastIssue + "," + issuesAssignedThisRepoCount + "," + issuesAssignedCount + "," + firstIssueAssignedThisRepo + "," + firstIssueAssigned + "," + lastIssueAssignedThisRepo + "," + lastIssueAssigned + "," + commitThisRepoCount + "," + commitCount + "," + firstCommitThisRepo + "," + firstCommit + "," + lastCommitThisRepo + "," + lastCommit + "," + prThisRepoCount + "," + prCount + "," + mergedPRThisRepoCount + "," + mergedPRCount + "," + firstPRThisRepo + "," + firstPR + "," + lastPRThisRepo + "," + lastPR + "," + commentsThisRepoCount + "," + commentsCount + "," + firstCommentThisRepo + "," + firstComment + "," + lastCommentThisRepo + "," + lastComment  
}

g = new Neo4jGraph("/home/kellyb/new_data/graph.db")
repos = ["rails/rails"]
repos.each{dumpRepositoryUsers(it, g)}
g.shutdown()
