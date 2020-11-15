package org.hawk.change;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.hawk.core.VcsChangeType;
import org.eclipse.hawk.core.VcsCommit;
import org.eclipse.hawk.core.VcsCommitItem;
import org.eclipse.hawk.core.VcsRepositoryDelta;
import org.eclipse.hawk.svn.SvnManager;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.io.SVNRepository;

public class ChangeSvnManager extends SvnManager{
	protected static final Set<String> EXTENSION_BLACKLIST = new HashSet<>(
			Arrays.asList(".png", ".jpg", ".bmp", ".jar", ".gz", ".tar"));

	@Override
	public VcsRepositoryDelta getDelta(String startRevision, String endRevision) throws Exception {
		final SVNRepository svnRepository = getSVNRepository();
		String  repositoryURL= getLocation();

		final List<VcsCommit> commits = new ArrayList<>();
		VcsRepositoryDelta delta = new VcsRepositoryDelta(commits);
		delta.setManager(this);

		final String rootURL = svnRepository.getRepositoryRoot(false).toDecodedString();
		final String overlappedURL = makeRelative(rootURL, repositoryURL);

		if (startRevision == null && endRevision != startRevision || !startRevision.equals(endRevision)) {
			Collection<?> c = svnRepository.log(new String[] { "" }, null,
					startRevision == null ? 0 : Long.valueOf(startRevision),
					Long.valueOf(endRevision), true, true);

			for (Object o : c) {
				SVNLogEntry svnLogEntry = (SVNLogEntry) o;
				VcsCommit commit = new VcsCommit();

				commit.setAuthor(svnLogEntry.getAuthor());
				commit.setMessage(svnLogEntry.getMessage());
				commit.setRevision(svnLogEntry.getRevision() + "");
				commit.setJavaDate(svnLogEntry.getDate());
				commits.add(commit);

				Map<String, SVNLogEntryPath> changedPaths = svnLogEntry.getChangedPaths();
				for (final String path : changedPaths.keySet()) {
					SVNLogEntryPath svnLogEntryPath = changedPaths.get(path);

					final int lastDotIndex = path.lastIndexOf(".");
					if (lastDotIndex <= 1) {
						// No extension (index is -1) or path starts by "/." (hidden files in Unix systems): skip
						continue;
					}
					final String ext = path.substring(lastDotIndex, path.length());
					if (EXTENSION_BLACKLIST.contains(ext)) {
						// Blacklisted extension: skip
						continue;
					}

					if (path.contains(overlappedURL)) {
						VcsCommitItem commitItem = new VcsCommitItem();
						commit.getItems().add(commitItem);
						commitItem.setCommit(commit);

						commitItem.setPath(path);

						if (svnLogEntryPath.getType() == 'A') {
							if(svnLogEntryPath.getCopyPath()!= null) {
								SVNLogEntryPath copySourcePath = (SVNLogEntryPath) changedPaths.get(svnLogEntryPath.getCopyPath());
								if (copySourcePath != null && copySourcePath.getType() == SVNLogEntryPath.TYPE_DELETED) {
									commitItem.setChangeType(VcsChangeType.MOVED);
									//System.out.println("Path " + path + " was moved from " + svnLogEntryPath.getCopyPath() + " in revision " + svnLogEntryPath.getRevision());
							    }
								else
									commitItem.setChangeType(VcsChangeType.RENAMED);
							}
							else
								commitItem.setChangeType(VcsChangeType.ADDED);
						} else if (svnLogEntryPath.getType() == 'M') {
							commitItem.setChangeType(VcsChangeType.UPDATED);
						} else if (svnLogEntryPath.getType() == 'D') {
							commitItem.setChangeType(VcsChangeType.DELETED);
						} else if (svnLogEntryPath.getType() == 'R') {
							commitItem.setChangeType(VcsChangeType.REPLACED);
						} else {
							//console.printerrln("Found unrecognised svn log entry type: " + svnLogEntryPath.getType());
							commitItem.setChangeType(VcsChangeType.UNKNOWN);
						}
					}
				}
			}
		}

		return delta;
	}
	protected String makeRelative(String base, String extension) {
		StringBuilder result = new StringBuilder();
		List<String> baseSegments = Arrays.asList(base.split("/"));
		String[] extensionSegments = extension.split("/");
		for (String ext : extensionSegments) {
			if (!baseSegments.contains(ext)) {
				result.append(extension.substring(extension.indexOf(ext)));
				break;
			}
		}
		return result.toString();
	}
}
