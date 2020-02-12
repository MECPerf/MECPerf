def parse_request(request):
        try:
            compact = str(request.args.get('compact'))
        except KeyError:
            compact = False

        try:
            keyword = str(request.args.get('keyword'))
        except KeyError:
            keyword = "None"

        try:
            likeKeyword = str(request.args.get('likeKeyword'))
        except KeyError:
            LikeKeyword = "None"

        try:
            json = str(request.args.get('json'))
        except KeyError:
            json = False

        try:
            fromInterval = str(request.args.get('from'))
        except KeyError:
            fromInterval = "None"

        try:
            toInterval = str(request.args.get('to'))
        except KeyError:
            toInterval = "None"
        try:
            command = str(request.args.get('command'))
        except KeyError:
            command = "None"
        try:
            direction = str(request.args.get('direction'))
        except KeyError:
            direction = "None"

        return compact, keyword, likeKeyword, json, fromInterval, toInterval, command, direction
        