prs <- read.table("Git-Miner-Analysis/salt.out", sep=",", header=TRUE)
attach(prs)
cor.test(prs$merged, prs$linked,method="p", alternative="g")

commentsB4_bin <- ifelse(subset(prs,linked>0)$commentsB4 > 0, 1, 0)
cor.test(subset(prs,linked>0)$merged, commentsB4_bin, method="p", alternative="g")
cor.test(subset(prs,linked>0)$merged, subset(prs,linked>0)$commentsB4, method="s", alternative="g")

cor.test(subset(prs,linked>0)$merged,  subset(prs,linked>0)$sameOwner, method="p", alternative="g")

mean(subset(subset(prs,linked>0),merged>0)$ownerCommentsB4)
mean(subset(subset(prs,linked>0),merged<1)$ownerCommentsB4)
cor.test(subset(prs,linked>0)$merged, subset(prs,linked>0)$ownerCommentsB4, method="s", alternative="g")
ownerCommentsB4_bin <- ifelse(subset(prs,linked>0)$ownerCommentsB4 > 0, 1, 0)
cor.test(subset(prs,linked>0)$merged, ownerCommentsB4_bin, method="p", alternative="g")


cor.test(subset(subset(prs,linked>0),merged>0)$pr_commits, subset(subset(prs,linked>0),merged>0)$mergerCommentsB4, method="s", alternative="g")
mergerCommentsB4_bin <- ifelse(subset(subset(prs,linked>0),merged>0)$mergerCommentsB4 >0 , 1, 0)
cor.test(subset(subset(prs,linked>0),merged>0)$pr_commits, mergerCommentsB4_bin, method="p", alternative="g")

cor.test(subset(subset(prs,linked>0),merged>0)$commentsB4, subset(subset(prs,linked>0),merged>0)$pr_comments, method="s", alternative="l")
cor.test(subset(subset(prs,linked>0),merged>0)$ownerCommentsB4, subset(subset(prs,linked>0),merged>0)$pr_comments, method="s", alternative="l")

cor.test(subset(subset(prs,linked>0),merged>0)$pr_comments, subset(subset(prs,linked>0),merged>0)$mergerCommentsB4, method="s", alternative="g")
cor.test(subset(subset(prs,linked>0),merged>0)$pr_comments, mergerCommentsB4_bin, method="p", alternative="g")


firsts <- subset(prs, first_pr == 0)
others <- subset(prs, first_pr > 0)
nrow(subset(firsts,merged>0))
nrow(subset(firsts,merged==0))
nrow(subset(others,merged>0))
nrow(subset(others,merged==0))
chisq.test(rbind(c(nrow(subset(firsts,merged>0)),nrow(subset(firsts,merged<1))),c(nrow(subset(others,merged>0)),nrow(subset(others,merged<1)))), correct=FALSE)

chisq.test(rbind(c(nrow(subset(firsts,sameOwner>0)),nrow(subset(firsts, sameOwner <1))),c(nrow(subset(others, sameOwner>0)),nrow(subset(others, sameOwner <1)))), correct=FALSE)

chisq.test(rbind(c(nrow(subset(firsts,ownerCommentsB4>0)),nrow(subset(firsts, ownerCommentsB4 <1))),c(nrow(subset(others, ownerCommentsB4>0)),nrow(subset(others, ownerCommentsB4 <1)))), correct=FALSE)

mean(subset(firsts,linked>0)$ownerCommentsB4)
mean(subset(others,linked>0)$ownerCommentsB4)
wilcox.test(subset(firsts,linked>0)$ownerCommentsB4,subset(others,linked>0)$ownerCommentsB4, alternative="l")

mean(subset(subset(firsts,linked>0),ownerCommentsB4>0)$ownerCommentsB4)
mean(subset(subset(others,linked>0),ownerCommentsB4>0)$ownerCommentsB4)
wilcox.test(subset(subset(firsts,linked>0),ownerCommentsB4>0)$ownerCommentsB4, subset(subset(others,linked>0),ownerCommentsB4>0)$ownerCommentsB4, alternative="l")
