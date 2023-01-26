# Plugins

## Plugin list

Plugin | Fonction
-- | --
`countAuthors` | count the number of authors
`countCommits` | counts the commits per authors
`countLines` | counts the number of added lines per author
`countRemovedLines` | counts the number of removed lines per author
`countLinesOverTime` | counts the number of added lines per day
`countMergeCommits` | counts the number of merge commits per author
`countContributionPercentage` | counts the percentage of contribution of each author
`countMostChangedFiles` | gets the most changed files

## Plugin options

### Options structure

There are three types of options:

- toggle options
- value options
- charts

#### The value options

They represent a key value options (i.e. width: 100).

A list of existing toggle options that are not specific to any plugin is:

Options | Type | What it means
--- | --- | ---
`displayName` | string | The name you want your plugin to show up as in the frontend
`width` | int | The width of the graph
`author` | string | A regular expression that should match the whole name of the author
`date` | string | A date interval to select commits

### The toggle options

They represent on and off values (i.e. nocolor).

A list of existins value options that are not specific to any plugins is:

There are currently no toggle options.

### The charts

This represents the different type of charts that are available.

The list of available charts are :

- bar
- pie
- doughnut
