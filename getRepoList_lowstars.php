<?php

  set_time_limit(0);

  downloadRepoList();

  function getRepoIds($page) {
    $fileMarkerStart = '<h3 class="repolist-name">';
    $fileMarkerEnd = '</h3>';
    $offset = 0;
    $ids = array();
    do {
        $pos1 = strpos($page, $fileMarkerStart, $offset);
        if ($pos1 !== false) {
            $offset = $pos1 + strlen($fileMarkerStart);
      	    $pos2 = strpos($page, $fileMarkerEnd, $offset);
      	    if ($pos2 !== false){
                $id = substr($page, $offset, $pos2-$offset);
                preg_match('/\"([^\"]*?)\"/',$id,$matches);
                $id = substr($matches[1],1);
                $ids[] = $id;
                $offset = $pos2 + strlen($fileMarkerEnd);
      	    }
        }
    } while ($pos1 !== false && $pos2 !== false);

    return $ids;
  }

  function downloadRepoList(){
    echo "starting...\n";
    $page = 1;
    do {
   
	$repolistQuery = 'https://github.com/search?p=' . $page . '&q=stars%3A%3C100+forks%3A%3E100+pushed%3A%3E2013-08-14&ref=advsearch&type=Repositories';

	$repoList = file_get_contents($repolistQuery);
        if ($repoList === false) {
            return; 
        }
        foreach(getRepoIds($repoList) as $r) {
            echo $r . ",";
        }
        $page++;
        sleep(20);
    }while (!empty($repoList));

  }

?>