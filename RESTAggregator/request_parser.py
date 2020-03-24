def parse_request(request):
        PARAMETERS = {}
        PARAMETERS["group_by"] = True
        try:
            PARAMETERS["compact"] = request.args.get('compact')
        except KeyError:
            PARAMETERS["compact"] = False

        try:
            PARAMETERS["keyword"] = request.args.get('keyword')
        except KeyError:
            PARAMETERS["keyword"] = None

        try:
            PARAMETERS["likeKeyword"] = request.args.get('likeKeyword')
        except KeyError:
            PARAMETERS["LikeKeyword"] = None

        try:
            PARAMETERS["json"] = str(request.args.get('json'))
        except KeyError:
            PARAMETERS["json"] = False

        try:
            PARAMETERS["fromInterval"] = request.args.get('from')
        except KeyError:
            PARAMETERS["fromInterval"] = None

        try:
            PARAMETERS["toInterval"] = request.args.get('to')
        except KeyError:
            PARAMETERS["toInterval"] = None
        try:
            PARAMETERS["command"] = request.args.get('command')
        except KeyError:
            PARAMETERS["command"] = None
        try:
            PARAMETERS["direction"] = request.args.get('direction')
        except KeyError:
            PARAMETERS["direction"] = None
        try:
            PARAMETERS["dashfilename"] = request.args.get('dashfilename')
        except KeyError:
            PARAMETERS["dashfilename"] = None
        try:
            PARAMETERS["numberofclients"] = request.args.get('clientnumber')
        except KeyError:
            PARAMETERS["numberofclients"] = None
        try:
            PARAMETERS["limit"] = request.args.get('limit')    
        except KeyError:
            PARAMETERS["limit"] = None
        try:
            PARAMETERS["offset"] = request.args.get('offset')    
        except KeyError:
            PARAMETERS["offset"] = None



        try:
            val = str(request.args.get('group_by'))

            if "true" == val or "True" == val:
                PARAMETERS["group_by"] = True
            if "false" == val or "False" == val:
                PARAMETERS["group_by"] = False
        except KeyError:
            PARAMETERS["group_by"] = True

        return PARAMETERS
        